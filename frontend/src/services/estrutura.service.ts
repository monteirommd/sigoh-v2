import { requisitar } from "./api";
import type { Bloco, Enfermaria } from "../types/models";

export interface BlocoInput {
  nome: string;
}

export interface EnfermariaInput {
  nome: string;
  blocoId: number;
}

/** Estrutura fisica do hospital: blocos e enfermarias. */
export const BlocoService = {
  listar: (): Promise<Bloco[]> => requisitar<Bloco[]>("/api/blocos"),
  criar: (input: BlocoInput): Promise<Bloco> =>
    requisitar<Bloco>("/api/blocos", { method: "POST", body: input }),
  atualizar: (id: number, input: BlocoInput): Promise<Bloco> =>
    requisitar<Bloco>(`/api/blocos/${id}`, { method: "PUT", body: input }),
  excluir: (id: number): Promise<void> =>
    requisitar<void>(`/api/blocos/${id}`, { method: "DELETE" }),
};

export const EnfermariaService = {
  listar: (blocoId?: number): Promise<Enfermaria[]> =>
    requisitar<Enfermaria[]>(blocoId ? `/api/enfermarias?blocoId=${blocoId}` : "/api/enfermarias"),
  criar: (input: EnfermariaInput): Promise<Enfermaria> =>
    requisitar<Enfermaria>("/api/enfermarias", { method: "POST", body: input }),
  atualizar: (id: number, input: EnfermariaInput): Promise<Enfermaria> =>
    requisitar<Enfermaria>(`/api/enfermarias/${id}`, { method: "PUT", body: input }),
  excluir: (id: number): Promise<void> =>
    requisitar<void>(`/api/enfermarias/${id}`, { method: "DELETE" }),
};
