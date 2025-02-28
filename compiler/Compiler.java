import java.util.*;
// Documenta este codigo:
//
// 1. Explica cómo funciona el servidor HTTP.
// 2. Explica cómo se tokeniza y evalúa la expresión matemática.
// 3. Explica cómo se manejan los errores en la expresión matemática.
// 4. Explica cómo se responde a las solicitudes CORS.
// 5. Explica cómo se responde a las solicitudes POST.
// 6. Explica cómo se responde a las solicitudes OPTIONS.
// 7. Explica cómo se envían las respuestas HTTP.
//
// 1. El servidor HTTP recibe una solicitud POST en la ruta /api/compile con un cuerpo JSON que
// 	contiene una expresión matemática. El servidor tokeniza la expresión y la evalúa para devolver
// 	el resultado en formato JSON. El servidor responde con un código de estado 200 si la expresión
// 	es válida y 400 si hay un error. El servidor también responde a solicitudes OPTIONS para permitir
// 	solicitudes CORS desde cualquier origen de UI.
//
// 	El servidor HTTP se inicia en el puerto 8080 y se crea un contexto en la ruta /api/compile que
//  maneja las solicitudes de compilación. El servidor se inicia y se queda a la espera de
// 	solicitudes entrantes.
//
// 	2. La expresión matemática se tokeniza y evalúa utilizando un Lexer y un Parser.
// 	el Lexer convierte la cadena de entrada en una lista de tokens, donde cada token
// 	epresenta un número, un operador o el final de la entrada. El Parser toma la
// 	ista de tokens y evalúa la expresión matemática siguiendo las reglas de
// 	recedencia de operadores.
//

// Token representation
class Token {
    enum Type { NUMBER, PLUS, MINUS, MULTIPLY, DIVIDE, EOF }
    Type type;
    String value;
    
    Token(Type type, String value) {
        this.type = type;
        this.value = value;
    }
}

// Lexer: Converts input string into tokens
class Lexer {
    private final String input;
    private int pos;

    Lexer(String input) {
        this.input = input;
        this.pos = 0;
    }

    private char currentChar() {
        return pos < input.length() ? input.charAt(pos) : '\0';
    }

    void advance() {
        pos++;
    }

    List<Token> tokenize() {
        List<Token> tokens = new ArrayList<>();

        while (pos < input.length()) {
            char current = currentChar();
            if (Character.isDigit(current)) {
                StringBuilder number = new StringBuilder();
                while (Character.isDigit(currentChar())) {
                    number.append(currentChar());
                    advance();
                }
                tokens.add(new Token(Token.Type.NUMBER, number.toString()));
            } else if (current == '+') {
                tokens.add(new Token(Token.Type.PLUS, "+"));
                advance();
            } else if (current == '-') {
                tokens.add(new Token(Token.Type.MINUS, "-"));
                advance();
            } else if (current == '*') {
                tokens.add(new Token(Token.Type.MULTIPLY, "*"));
                advance();
            } else if (current == '/') {
                tokens.add(new Token(Token.Type.DIVIDE, "/"));
                advance();
            } else {
                advance(); // Ignore spaces
            }
        }
        tokens.add(new Token(Token.Type.EOF, ""));
        return tokens;
    }
}

// Parser & Evaluator
class Parser {
    private final List<Token> tokens;
    private int pos;

    Parser(List<Token> tokens) {
        this.tokens = tokens;
        this.pos = 0;
    }

    private Token currentToken() {
        return tokens.get(pos);
    }

    private void consume(Token.Type type) {
        if (currentToken().type == type) {
            pos++;
        } else {
            throw new RuntimeException("Unexpected token: " + currentToken().value);
        }
    }

    private int factor() {
        Token token = currentToken();
        consume(Token.Type.NUMBER);
        return Integer.parseInt(token.value);
    }

    private int term() {
        int result = factor();
        while (currentToken().type == Token.Type.MULTIPLY || currentToken().type == Token.Type.DIVIDE) {
            if (currentToken().type == Token.Type.MULTIPLY) {
                consume(Token.Type.MULTIPLY);
                result *= factor();
            } else {
                consume(Token.Type.DIVIDE);
                result /= factor();
            }
        }
        return result;
    }

    public int expression() {
        int result = term();
        while (currentToken().type == Token.Type.PLUS || currentToken().type == Token.Type.MINUS) {
            if (currentToken().type == Token.Type.PLUS) {
                consume(Token.Type.PLUS);
                result += term();
            } else {
                consume(Token.Type.MINUS);
                result -= term();
            }
        }
        return result;
    }
}

// Main class to test
public class Compiler {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter an expression: ");
        String input = scanner.nextLine();
        scanner.close();

        Lexer lexer = new Lexer(input);
        List<Token> tokens = lexer.tokenize();
        Parser parser = new Parser(tokens);
        int result = parser.expression();

        System.out.println("Result: " + result);
    }
}
