package com.sigoh.api.leito.dto;

import com.sigoh.api.common.enums.StatusLeito;

import java.time.Instant;

public record LeitoResponseDTO(
        Long id,
        String codigo,
        Long enfermariaId,
        String enfermariaNome,
        Long blocoId,
        String blocoNome,
        StatusLeito status,
        boolean possuiOxigenio,
        boolean isolamento,
        Instant atualizadoEm,
        String atualizadoPor
) {
}
