import { createContext, useCallback, useContext, useMemo, useState, type ReactNode } from "react";
import { definirToken } from "../services/api";
import { AuthService } from "../services/auth.service";
import type { Role } from "../types/models";

interface UsuarioAutenticado {
  id: number;
  nome: string;
  role: Role;
}

interface AuthContextValue {
  usuario: UsuarioAutenticado | null;
  isAdmin: boolean;
  entrar: (email: string, senha: string) => Promise<void>;
  sair: () => void;
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined);

/**
 * Estado de autenticacao compartilhado por toda a aplicacao.
 *
 * O token e mantido em memoria (nao em localStorage): reduz a superficie de
 * ataque por XSS ao custo de exigir novo login ao recarregar a pagina.
 * Persistir a sessao e um refinamento previsto, nao um esquecimento.
 */
export function AuthProvider({ children }: { children: ReactNode }) {
  const [usuario, setUsuario] = useState<UsuarioAutenticado | null>(null);

  const entrar = useCallback(async (email: string, senha: string) => {
    const resposta = await AuthService.login(email, senha);
    definirToken(resposta.token);
    setUsuario({ id: resposta.usuarioId, nome: resposta.nome, role: resposta.role });
  }, []);

  const sair = useCallback(() => {
    definirToken(null);
    setUsuario(null);
  }, []);

  const valor = useMemo<AuthContextValue>(
    () => ({ usuario, isAdmin: usuario?.role === "ADMIN", entrar, sair }),
    [usuario, entrar, sair],
  );

  return <AuthContext.Provider value={valor}>{children}</AuthContext.Provider>;
}

export function useAuth(): AuthContextValue {
  const contexto = useContext(AuthContext);
  if (!contexto) {
    throw new Error("useAuth deve ser usado dentro de AuthProvider");
  }
  return contexto;
}
