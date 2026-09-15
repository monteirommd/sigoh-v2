import { useCallback, useEffect, useState } from "react";
import { useAuth } from "../context/AuthContext";
import { useOcupacaoRealtime } from "../hooks/useOcupacaoRealtime";
import { LeitoService } from "../services/leito.service";
import { OcupacaoService } from "../services/ocupacao.service";
import type { Leito, LeitoAtualizadoEvent, OcupacaoBloco, StatusLeito } from "../types/models";
import { ROTULO_STATUS, STATUS_LEITO } from "../types/models";

/**
 * Tela principal: mapa de ocupação.
 *
 * Usuário comum apenas consulta; administrador altera o status direto daqui
 * (ação de ronda). As duas visões usam a mesma listagem — o que muda é a
 * presença dos controles de alteração.
 */
export function MapaLeitos() {
  const { isAdmin } = useAuth();

  const [leitos, setLeitos] = useState<Leito[]>([]);
  const [ocupacao, setOcupacao] = useState<OcupacaoBloco[]>([]);
  const [carregando, setCarregando] = useState(true);
  const [erro, setErro] = useState<string | null>(null);

  const carregar = useCallback(async () => {
    setErro(null);
    try {
      const [listaLeitos, listaOcupacao] = await Promise.all([
        LeitoService.listar(),
        OcupacaoService.porBloco(),
      ]);
      setLeitos(listaLeitos);
      setOcupacao(listaOcupacao);
    } catch (e) {
      setErro(e instanceof Error ? e.message : "Erro ao carregar o mapa de leitos");
    } finally {
      setCarregando(false);
    }
  }, []);

  useEffect(() => {
    void carregar();
  }, [carregar]);

  // Quando outro usuário altera um leito, a tela reflete a mudança sem recarregar.
  const aoReceberEvento = useCallback(
    (evento: LeitoAtualizadoEvent) => {
      setLeitos((atuais) =>
        atuais.map((l) => (l.id === evento.leitoId ? { ...l, status: evento.statusNovo } : l)),
      );
      void OcupacaoService.porBloco().then(setOcupacao).catch(() => undefined);
    },
    [],
  );
  useOcupacaoRealtime(aoReceberEvento);

  async function alterarStatus(leito: Leito, novoStatus: StatusLeito) {
    setErro(null);
    try {
      const atualizado = await LeitoService.atualizarStatus(leito.id, novoStatus);
      setLeitos((atuais) => atuais.map((l) => (l.id === atualizado.id ? atualizado : l)));
      setOcupacao(await OcupacaoService.porBloco());
    } catch (e) {
      setErro(e instanceof Error ? e.message : "Erro ao atualizar o leito");
    }
  }

  if (carregando) {
    return <p>Carregando mapa de leitos...</p>;
  }

  return (
    <>
      <section aria-labelledby="titulo-ocupacao">
        <h1 id="titulo-ocupacao">Ocupação por bloco</h1>

        {erro && (
          <p className="mensagem-erro" role="alert">
            {erro}
          </p>
        )}

        <div className="grade-indicadores">
          {ocupacao.map((bloco) => (
            <article key={bloco.blocoId} className="indicador">
              <h2>{bloco.blocoNome}</h2>
              <p className="indicador-valor">{bloco.taxaOcupacao}%</p>
              <p className="indicador-detalhe">
                {bloco.ocupados} ocupados de {bloco.totalLeitos} leitos
              </p>
            </article>
          ))}
        </div>
      </section>

      <section aria-labelledby="titulo-leitos">
        <h2 id="titulo-leitos">Leitos</h2>

        <table>
          <caption className="apenas-leitor-tela">
            Relação de leitos com bloco, enfermaria e status atual
          </caption>
          <thead>
            <tr>
              <th scope="col">Código</th>
              <th scope="col">Bloco</th>
              <th scope="col">Enfermaria</th>
              <th scope="col">Status</th>
              {isAdmin && <th scope="col">Alterar status</th>}
            </tr>
          </thead>
          <tbody>
            {leitos.map((leito) => (
              <tr key={leito.id}>
                <td>{leito.codigo}</td>
                <td>{leito.blocoNome}</td>
                <td>{leito.enfermariaNome}</td>
                <td>
                  <span className={`etiqueta etiqueta-${leito.status.toLowerCase()}`}>
                    {ROTULO_STATUS[leito.status]}
                  </span>
                </td>
                {isAdmin && (
                  <td>
                    <label className="apenas-leitor-tela" htmlFor={`status-${leito.id}`}>
                      Novo status do leito {leito.codigo}
                    </label>
                    <select
                      id={`status-${leito.id}`}
                      value={leito.status}
                      onChange={(e) => void alterarStatus(leito, e.target.value as StatusLeito)}
                    >
                      {STATUS_LEITO.map((status) => (
                        <option key={status} value={status}>
                          {ROTULO_STATUS[status]}
                        </option>
                      ))}
                    </select>
                  </td>
                )}
              </tr>
            ))}
          </tbody>
        </table>
      </section>
    </>
  );
}
