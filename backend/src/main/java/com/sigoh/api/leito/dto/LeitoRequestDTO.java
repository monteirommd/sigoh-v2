package com.sigoh.api.leito.dto;

import com.sigoh.api.common.enums.StatusLeito;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record LeitoRequestDTO(
        @NotBlank(message = "e obrigatorio") @Size(max = 30) String codigo,
        @NotNull(message = "e obrigatoria") Long enfermariaId,
        @NotNull(message = "e obrigatorio") StatusLeito status,
        boolean possuiOxigenio,
        boolean isolamento
) {
}
