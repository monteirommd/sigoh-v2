package com.sigoh.api.leito.dto;

import com.sigoh.api.common.enums.StatusLeito;
import jakarta.validation.constraints.NotNull;

/** Corpo da acao operacional de ronda: apenas o novo status. */
public record AtualizarStatusDTO(
        @NotNull(message = "e obrigatorio") StatusLeito novoStatus
) {
}
