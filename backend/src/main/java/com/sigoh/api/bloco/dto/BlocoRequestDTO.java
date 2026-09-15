package com.sigoh.api.bloco.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record BlocoRequestDTO(
        @NotBlank(message = "e obrigatorio") @Size(max = 120) String nome
) {
}
