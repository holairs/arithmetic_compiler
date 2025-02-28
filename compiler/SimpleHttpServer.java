import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.*;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

// Infomrmación de uso:

// Este archivo contiene un servidor HTTP que recibe una solicitud POST en la ruta /api/compile
// con un cuerpo JSON que contiene una expresión matemática. El servidor tokeniza la expresión
// y la evalúa para devolver el resultado en formato JSON. El servidor responde con un código
// de estado 200 si la expresión es válida y 400 si hay un error. El servidor también responde
// a solicitudes OPTIONS para permitir solicitudes CORS desde cualquier origen de UI.

public class SimpleHttpServer {
	public static void main(String[] args) throws IOException {
		HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
		server.createContext("/api/compile", new CompileHandler());
		server.setExecutor(null);
		System.out.println("Server started on port 8080...");
		server.start();
	}
}

class CompileHandler implements HttpHandler {
	private final Gson gson = new Gson();

	@Override
	public void handle(HttpExchange exchange) throws IOException {
		if ("OPTIONS".equals(exchange.getRequestMethod())) {
			// Responder solicitud preflight (CORS)
			exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
			exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
			exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
			exchange.sendResponseHeaders(204, -1); // No content
			return;
		}

		// Leer el cuerpo de la solicitud
		// Ejemplo de cuerpo JSON: {"code": "2 + 3 * 4"}
		// Ejemplo de cuerpo JSON: {"code": "2 + 3 * 4 / 2"}
		//  
		//  Este cuerpo solo recibe una propiedad llamada "code" que contiene la expresión
		//  matemática a evaluar. La expresión debe contener números enteros y los operadores
		//  +, -, *, /.
		//
		//  Y solo puede ser de tipo POST
		//  Si la expresión es válida, el servidor responde con un código de estado 200 y un
		//  JSON que contiene el resultado de la expresión. Si hay un error en la expresión,
		//  el servidor responde con un código de estado 400 y un JSON que contiene un mensaje
		//  de error.
		//

		if ("POST".equals(exchange.getRequestMethod())) {
			byte[] requestBody = exchange.getRequestBody().readAllBytes();
			String body = new String(requestBody);

			Gson gson = new Gson();
			JsonObject json = gson.fromJson(body, JsonObject.class);
			String expression = json.get("code").getAsString();

			try {
				Lexer lexer = new Lexer(expression);
				List<Token> tokens = lexer.tokenize();
				Parser parser = new Parser(tokens);
				int result = parser.expression();

				JsonObject response = new JsonObject();
				response.addProperty("result", result);

				sendResponse(exchange, 200, response.toString());
			} catch (Exception e) {
				sendResponse(exchange, 400, "{\"error\": \"Error en la expresión\"}");
			}
		} else {
			sendResponse(exchange, 405, "{\"error\": \"Método no permitido\"}");
		}
	}

	// Esto es un método auxiliar para enviar una respuesta HTTP
	// con un código de estado y un cuerpo de respuesta.
	//
	// Permite recibir peticiónes de tipo GET, POST y OPTIONS
	// desde cualquier origen de UI.

	private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
		exchange.getResponseHeaders().set("Content-Type", "application/json");                   // 🔥 Responder con JSON
		exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");                   // 🔥 Permitir solicitudes desde cualquier origen de UI
		exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS"); // 🔥 Permitir solicitudes GET, POST y OPTIONS
		exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");       // 🔥 Permitir solicitudes con Content-Type

		exchange.sendResponseHeaders(statusCode, response.length());
		OutputStream os = exchange.getResponseBody();
		os.write(response.getBytes());
		os.close();
	}
}
