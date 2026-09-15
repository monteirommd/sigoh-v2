import type { ReactNode } from "react";
import { Navigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import type { Role } from "../types/models";

interface Props {
  children: ReactNode;
  rolesPermitidos?: Role[];
}

/**
 * Barreira de navegacao no cliente. E conveniencia de interface: a autorizacao
 * efetiva acontece no servidor, a cada requisicao.
 */
export function RotaProtegida({ children, rolesPermitidos }: Props) {
  const { usuario } = useAuth();

  if (!usuario) {
    return <Navigate to="/login" replace />;
  }

  if (rolesPermitidos && !rolesPermitidos.includes(usuario.role)) {
    return <Navigate to="/mapa" replace />;
  }

  return <>{children}</>;
}
