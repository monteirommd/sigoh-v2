package com.sigoh.api.enfermaria.dto;

public record EnfermariaResponseDTO(Long id, String nome, Long blocoId, String blocoNome, boolean ativo) {
}
