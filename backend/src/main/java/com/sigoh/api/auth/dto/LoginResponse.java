package com.sigoh.api.auth.dto;

import com.sigoh.api.common.enums.Role;

public record LoginResponse(String token, Long usuarioId, String nome, Role role) {
}
