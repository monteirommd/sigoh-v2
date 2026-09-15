package com.sigoh.api.realtime.dto;

import com.sigoh.api.common.enums.StatusLeito;

import java.time.Instant;

/** Mensagem publicada em /topic/ocupacao a cada mudanca de status de leito. */
public record LeitoAtualizadoEvent(
        Long leitoId,
        String codigo,
        Long enfermariaId,
        Long blocoId,
        StatusLeito statusAnterior,
        StatusLeito statusNovo,
        String atualizadoPor,
        Instant momento
) {
}
