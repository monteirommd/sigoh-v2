package com.sigoh.api.auth;

import com.sigoh.api.auth.dto.LoginRequest;
import com.sigoh.api.auth.dto.LoginResponse;
import com.sigoh.api.config.JwtService;
import com.sigoh.api.usuario.Usuario;
import jakarta.validation.Valid;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/** Unico endpoint publico da API. */
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthController(AuthenticationManager authenticationManager, JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        var autenticacao = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.senha()));

        Usuario usuario = (Usuario) autenticacao.getPrincipal();

        // O papel vai como claim para o front-end poder ajustar a interface.
        // A autorizacao real continua sendo feita no servidor a cada requisicao.
        String token = jwtService.gerarToken(usuario.getEmail(), Map.of("role", usuario.getRole().name()));

        return new LoginResponse(token, usuario.getId(), usuario.getNome(), usuario.getRole());
    }
}
