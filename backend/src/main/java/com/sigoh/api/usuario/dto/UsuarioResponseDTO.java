package com.sigoh.api.usuario.dto;

import com.sigoh.api.common.enums.Role;

public record UsuarioResponseDTO(Long id, String nome, String email, Role role, boolean ativo) {
}
