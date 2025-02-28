import { useState } from 'react';
import './App.css';

function App() {
	const [inputCode, setInputCode] = useState('');
	const [outputResult, setOutputResult] = useState('');
	const [isLoading, setIsLoading] = useState(false);
	const [error, setError] = useState(null);

	const handleCompile = async () => {
		if (!inputCode.trim()) {
			setError('Por favor ingresa código para compilar');
			return;
		}

		setIsLoading(true);
		setError(null);

		try {
			//Esta es la estructura para cuando tengas el backend
			const response = await fetch('http://localhost:8080/api/compile', {
				method: 'POST',
				headers: {
					'Content-Type': 'application/json',
				},
				body: JSON.stringify({ code: inputCode }),
			});

			if (!response.ok) {
				throw new Error('Error en la compilación');
			}

			const data = await response.json();
			setOutputResult(data.result);

		} catch (err) {
			setError('Error al comunicarse con el servidor: ' + err.message);
			setIsLoading(false);
		} finally {
			setIsLoading(false);
		}
	};

	const handleClear = () => {
		setInputCode('');
		setOutputResult('');
		setError(null);
	};

	return (
		<div className="app-container">
			<header>
				<h1>Compilador <span className="accent">React</span></h1>
			</header>

			<main>
				<div className="editor-container">
					<div className="input-section">
						<div className="section-header">
							<h2>Código Fuente</h2>
							<button
								className="clear-btn"
								onClick={handleClear}
								disabled={isLoading}
							>
								Limpiar
							</button>
						</div>
						<textarea
							value={inputCode}
							onChange={(e) => setInputCode(e.target.value)}
							placeholder="Ingresa tu código aquí..."
							disabled={isLoading}
						/>
					</div>

					<div className="action-section">
						<button
							className="compile-btn"
							onClick={handleCompile}
							disabled={isLoading}
						>
							{isLoading ? 'Compilando...' : 'Compilar'}
						</button>
					</div>

					<div className="output-section">
						<h2>Resultado</h2>
						{error && <div className="error-message">{error}</div>}
						<pre className="output-area">
							{outputResult || 'El resultado de la compilación aparecerá aquí...'}
						</pre>
					</div>
				</div>
			</main>

			<footer>
				<p>Proyecto de Compilador © {new Date().getFullYear()}</p>
			</footer>
		</div>
	);
}

export default App;
