import { useState, type FormEvent } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

export function Login() {
  const { entrar } = useAuth();
  const navegar = useNavigate();

  const [email, setEmail] = useState("");
  const [senha, setSenha] = useState("");
  const [erro, setErro] = useState<string | null>(null);
  const [carregando, setCarregando] = useState(false);

  async function handleSubmit(evento: FormEvent) {
    evento.preventDefault();
    setErro(null);
    setCarregando(true);
    try {
      await entrar(email, senha);
      navegar("/mapa");
    } catch (e) {
      setErro(e instanceof Error ? e.message : "Não foi possível entrar");
    } finally {
      setCarregando(false);
    }
  }

  return (
    <main className="tela-login">
      <div className="cartao-login">
        <h1>SiGOH</h1>
        <p className="legenda">Sistema de Gestão de Ocupação Hospitalar</p>

        <form onSubmit={handleSubmit} noValidate>
          <div className="campo">
            <label htmlFor="email">E-mail</label>
            <input
              id="email"
              type="email"
              autoComplete="username"
              required
              value={email}
              onChange={(e) => setEmail(e.target.value)}
            />
          </div>

          <div className="campo">
            <label htmlFor="senha">Senha</label>
            <input
              id="senha"
              type="password"
              autoComplete="current-password"
              required
              value={senha}
              onChange={(e) => setSenha(e.target.value)}
            />
          </div>

          {/* role="alert" faz leitores de tela anunciarem o erro assim que surge */}
          {erro && (
            <p className="mensagem-erro" role="alert">
              {erro}
            </p>
          )}

          <button type="submit" className="botao-primario" disabled={carregando}>
            {carregando ? "Entrando..." : "Entrar"}
          </button>
        </form>
      </div>
    </main>
  );
}
