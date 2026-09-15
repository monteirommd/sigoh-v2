import { useEffect } from "react";
import { Client } from "@stomp/stompjs";
import type { LeitoAtualizadoEvent } from "../types/models";

const WS_URL = import.meta.env.VITE_WS_URL ?? "ws://localhost:8080/ws";

/**
 * Assina o canal de tempo real do back-end e executa um callback a cada
 * mudanca de status de leito.
 *
 * Sem isso, duas pessoas olhando o mapa ao mesmo tempo veriam estados
 * diferentes ate alguem recarregar a pagina. A conexao e encerrada
 * automaticamente ao desmontar o componente.
 */
export function useOcupacaoRealtime(aoAtualizar: (evento: LeitoAtualizadoEvent) => void): void {
  useEffect(() => {
    const client = new Client({
      brokerURL: WS_URL,
      reconnectDelay: 5000,
      onConnect: () => {
        client.subscribe("/topic/ocupacao", (mensagem) => {
          aoAtualizar(JSON.parse(mensagem.body) as LeitoAtualizadoEvent);
        });
      },
    });

    client.activate();
    return () => {
      void client.deactivate();
    };
  }, [aoAtualizar]);
}
