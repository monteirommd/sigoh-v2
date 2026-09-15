import { useCallback, useEffect, useState, type FormEvent } from "react";
import { BlocoService } from "../services/estrutura.service";
import type { Bloco } from "../types/models";

/**
 * Administração da estrutura física — CRUD completo de blocos.
 *
 * Rota restrita a administradores tanto na navegação (RotaProtegida) quanto
 * no servidor (SecurityConfig). Enfermarias e leitos seguem exatamente este
 * mesmo padrão e são a próxima tela a ser construída sobre esta base.
 */
export function GestaoEstrutura() {
  const [blocos, setBlocos] = useState<Bloco[]>([]);
  const [carregando, setCarregando] = useState(true);
  const [erro, setErro] = useState<string | null>(null);

  const [nome, setNome] = useState("");
  const [editandoId, setEditandoId] = useState<number | null>(null);

  const carregar = useCallback(async () => {
    setErro(null);
    try {
      setBlocos(await BlocoService.listar());
    } catch (e) {
      setErro(e instanceof Error ? e.message : "Erro ao carregar blocos");
    } finally {
      setCarregando(false);
    }
  }, []);

  useEffect(() => {
    void carregar();
  }, [carregar]);

  function limparFormulario() {
    setEditandoId(null);
    setNome("");
  }

  async function handleSubmit(evento: FormEvent) {
    evento.preventDefault();
    setErro(null);
    try {
      if (editandoId !== null) {
        await BlocoService.atualizar(editandoId, { nome });
      } else {
        await BlocoService.criar({ nome });
      }
      limparFormulario();
      await carregar();
    } catch (e) {
      setErro(e instanceof Error ? e.message : "Erro ao salvar o bloco");
    }
  }

  async function excluir(bloco: Bloco) {
    setErro(null);
    try {
      await BlocoService.excluir(bloco.id);
      await carregar();
    } catch (e) {
      // O servidor recusa excluir bloco com enfermarias vinculadas (HTTP 422);
      // a mensagem exibida aqui vem de lá.
      setErro(e instanceof Error ? e.message : "Erro ao excluir o bloco");
    }
  }

  return (
    <section aria-labelledby="titulo-estrutura">
      <h1 id="titulo-estrutura">Estrutura — Blocos</h1>

      {erro && (
        <p className="mensagem-erro" role="alert">
          {erro}
        </p>
      )}

      <form onSubmit={handleSubmit} aria-label={editandoId ? "Editar bloco" : "Cadastrar bloco"}>
        <div className="campo">
          <label htmlFor="nome-bloco">Nome do bloco</label>
          <input
            id="nome-bloco"
            required
            maxLength={120}
            value={nome}
            onChange={(e) => setNome(e.target.value)}
            placeholder="ex.: Bloco A"
          />
        </div>

        <div className="acoes-formulario">
          <button type="submit" className="botao-primario">
            {editandoId !== null ? "Salvar alterações" : "Cadastrar"}
          </button>
          {editandoId !== null && (
            <button type="button" className="botao-secundario" onClick={limparFormulario}>
              Cancelar
            </button>
          )}
        </div>
      </form>

      {carregando ? (
        <p>Carregando blocos...</p>
      ) : (
        <table>
          <caption className="apenas-leitor-tela">Blocos cadastrados</caption>
          <thead>
            <tr>
              <th scope="col">Nome</th>
              <th scope="col">Ações</th>
            </tr>
          </thead>
          <tbody>
            {blocos.map((bloco) => (
              <tr key={bloco.id}>
                <td>{bloco.nome}</td>
                <td className="celula-acoes">
                  <button
                    type="button"
                    className="botao-secundario"
                    aria-label={`Editar bloco ${bloco.nome}`}
                    onClick={() => {
                      setEditandoId(bloco.id);
                      setNome(bloco.nome);
                    }}
                  >
                    Editar
                  </button>
                  <button
                    type="button"
                    className="botao-perigo"
                    aria-label={`Excluir bloco ${bloco.nome}`}
                    onClick={() => void excluir(bloco)}
                  >
                    Excluir
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </section>
  );
}
