import { requisitar } from "./api";
import type { HistoricoLeito, Leito, StatusLeito } from "../types/models";

export interface LeitoInput {
  codigo: string;
  enfermariaId: number;
  status: StatusLeito;
  possuiOxigenio: boolean;
  isolamento: boolean;
}

export const LeitoService = {
  listar(filtros: { blocoId?: number; enfermariaId?: number } = {}): Promise<Leito[]> {
    const params = new URLSearchParams();
    if (filtros.blocoId) params.set("blocoId", String(filtros.blocoId));
    if (filtros.enfermariaId) params.set("enfermariaId", String(filtros.enfermariaId));
    const query = params.toString();
    return requisitar<Leito[]>(`/api/leitos${query ? `?${query}` : ""}`);
  },

  criar: (input: LeitoInput): Promise<Leito> =>
    requisitar<Leito>("/api/leitos", { method: "POST", body: input }),

  atualizar: (id: number, input: LeitoInput): Promise<Leito> =>
    requisitar<Leito>(`/api/leitos/${id}`, { method: "PUT", body: input }),

  /** Acao operacional: muda apenas o status, gerando registro de auditoria. */
  atualizarStatus: (id: number, novoStatus: StatusLeito): Promise<Leito> =>
    requisitar<Leito>(`/api/leitos/${id}/status`, { method: "PATCH", body: { novoStatus } }),

  excluir: (id: number): Promise<void> =>
    requisitar<void>(`/api/leitos/${id}`, { method: "DELETE" }),

  historico: (id: number): Promise<HistoricoLeito[]> =>
    requisitar<HistoricoLeito[]>(`/api/leitos/${id}/historico`),
};
