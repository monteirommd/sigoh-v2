// Contratos de dados da API. Espelham os DTOs do back-end (com.sigoh.api.*.dto)
// e sao a unica fonte de verdade de tipos no front-end.

export type Role = "USER" | "ADMIN";

export type StatusLeito = "DISPONIVEL" | "OCUPADO" | "LIMPEZA" | "BLOQUEADO";

export const STATUS_LEITO: StatusLeito[] = ["DISPONIVEL", "OCUPADO", "LIMPEZA", "BLOQUEADO"];

export const ROTULO_STATUS: Record<StatusLeito, string> = {
  DISPONIVEL: "Disponível",
  OCUPADO: "Ocupado",
  LIMPEZA: "Em limpeza",
  BLOQUEADO: "Bloqueado",
};

export interface Bloco {
  id: number;
  nome: string;
  ativo: boolean;
}

export interface Enfermaria {
  id: number;
  nome: string;
  blocoId: number;
  blocoNome: string;
  ativo: boolean;
}

export interface Leito {
  id: number;
  codigo: string;
  enfermariaId: number;
  enfermariaNome: string;
  blocoId: number;
  blocoNome: string;
  status: StatusLeito;
  possuiOxigenio: boolean;
  isolamento: boolean;
  atualizadoEm: string;
  atualizadoPor?: string;
}

export interface OcupacaoBloco {
  blocoId: number;
  blocoNome: string;
  totalLeitos: number;
  ocupados: number;
  disponiveis: number;
  emLimpeza: number;
  bloqueados: number;
  taxaOcupacao: number;
}

export interface HistoricoLeito {
  id: number;
  statusAnterior: StatusLeito;
  statusNovo: StatusLeito;
  usuarioNome: string;
  registradoEm: string;
}

export interface LoginResponse {
  token: string;
  usuarioId: number;
  nome: string;
  role: Role;
}

/** Evento recebido pelo canal de tempo real (/topic/ocupacao). */
export interface LeitoAtualizadoEvent {
  leitoId: number;
  codigo: string;
  enfermariaId: number;
  blocoId: number;
  statusAnterior: StatusLeito;
  statusNovo: StatusLeito;
  atualizadoPor: string;
  momento: string;
}
