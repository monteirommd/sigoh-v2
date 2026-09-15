import { requisitar } from "./api";
import type { LoginResponse } from "../types/models";

export const AuthService = {
  login(email: string, senha: string): Promise<LoginResponse> {
    return requisitar<LoginResponse>("/auth/login", { method: "POST", body: { email, senha } });
  },
};
