package com.sigoh.api.historico.dto;

import com.sigoh.api.common.enums.StatusLeito;

import java.time.Instant;

public record HistoricoLeitoResponseDTO(
        Long id,
        StatusLeito statusAnterior,
        StatusLeito statusNovo,
        String usuarioNome,
        Instant registradoEm
) {
}
