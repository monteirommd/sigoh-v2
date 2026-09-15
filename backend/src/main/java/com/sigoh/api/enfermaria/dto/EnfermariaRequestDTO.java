package com.sigoh.api.enfermaria.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record EnfermariaRequestDTO(
        @NotBlank(message = "e obrigatorio") @Size(max = 120) String nome,
        @NotNull(message = "e obrigatorio") Long blocoId
) {
}
