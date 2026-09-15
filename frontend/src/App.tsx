import { BrowserRouter, Navigate, Route, Routes } from "react-router-dom";
import { AuthProvider } from "./context/AuthContext";
import { RotaProtegida } from "./routes/RotaProtegida";
import { Layout } from "./pages/Layout";
import { Login } from "./pages/Login";
import { MapaLeitos } from "./pages/MapaLeitos";
import { GestaoEstrutura } from "./pages/GestaoEstrutura";

export function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <Routes>
          <Route path="/login" element={<Login />} />

          <Route
            element={
              <RotaProtegida>
                <Layout />
              </RotaProtegida>
            }
          >
            {/* Consulta: qualquer usuario autenticado */}
            <Route path="/mapa" element={<MapaLeitos />} />

            {/* Administracao: apenas ADMIN */}
            <Route
              path="/estrutura"
              element={
                <RotaProtegida rolesPermitidos={["ADMIN"]}>
                  <GestaoEstrutura />
                </RotaProtegida>
              }
            />
          </Route>

          <Route path="*" element={<Navigate to="/mapa" replace />} />
        </Routes>
      </AuthProvider>
    </BrowserRouter>
  );
}

export default App;
