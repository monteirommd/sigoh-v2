package com.sigoh.api.realtime;

import com.sigoh.api.realtime.dto.LeitoAtualizadoEvent;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

/** Publica eventos de ocupacao para todos os clientes conectados. */
@Service
public class OcupacaoBroadcaster {

    public static final String TOPICO_OCUPACAO = "/topic/ocupacao";

    private final SimpMessagingTemplate messagingTemplate;

    public OcupacaoBroadcaster(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void leitoAtualizado(LeitoAtualizadoEvent evento) {
        messagingTemplate.convertAndSend(TOPICO_OCUPACAO, evento);
    }
}
