import { NavLink, Outlet } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

/** Estrutura comum das telas autenticadas, com marcação semântica. */
export function Layout() {
  const { usuario, isAdmin, sair } = useAuth();

  return (
    <div className="aplicacao">
      <header className="cabecalho">
        <span className="marca">SiGOH</span>

        <nav aria-label="Navegação principal">
          <NavLink to="/mapa">Mapa de leitos</NavLink>
          {isAdmin && <NavLink to="/estrutura">Estrutura</NavLink>}
        </nav>

        <div className="identificacao">
          <span>
            {usuario?.nome} · {isAdmin ? "Administrador" : "Usuário"}
          </span>
          <button type="button" className="botao-secundario" onClick={sair}>
            Sair
          </button>
        </div>
      </header>

      <main className="conteudo">
        <Outlet />
      </main>

      <footer className="rodape">
        <small>SiGOH — Sistema de Gestão de Ocupação Hospitalar</small>
      </footer>
    </div>
  );
}
