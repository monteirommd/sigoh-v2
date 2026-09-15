// Cliente HTTP unico da aplicacao. Centralizar aqui a URL base, o cabecalho de
// autenticacao e o tratamento de erro evita que cada tela reimplemente isso de
// um jeito diferente.

const BASE_URL = import.meta.env.VITE_API_URL ?? "http://localhost:8080";

let tokenAtual: string | null = null;

export function definirToken(token: string | null): void {
  tokenAtual = token;
}

export function obterToken(): string | null {
  return tokenAtual;
}

interface OpcoesRequisicao {
  method?: "GET" | "POST" | "PUT" | "PATCH" | "DELETE";
  body?: unknown;
}

/** Formato de erro devolvido pelo GlobalExceptionHandler do back-end. */
interface ApiErrorBody {
  message?: string;
  status?: number;
}

export async function requisitar<T>(caminho: string, opcoes: OpcoesRequisicao = {}): Promise<T> {
  const headers: Record<string, string> = { "Content-Type": "application/json" };
  if (tokenAtual) {
    headers.Authorization = `Bearer ${tokenAtual}`;
  }

  const resposta = await fetch(`${BASE_URL}${caminho}`, {
    method: opcoes.method ?? "GET",
    headers,
    body: opcoes.body !== undefined ? JSON.stringify(opcoes.body) : undefined,
  });

  if (!resposta.ok) {
    const corpo: ApiErrorBody | null = await resposta.json().catch(() => null);
    throw new Error(corpo?.message ?? `Falha na requisição (HTTP ${resposta.status})`);
  }

  // 204 No Content nao tem corpo para desserializar.
  if (resposta.status === 204) {
    return undefined as T;
  }

  return (await resposta.json()) as T;
}
