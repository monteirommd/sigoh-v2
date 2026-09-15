package com.sigoh.api.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;

/**
 * Emissao e verificacao de JWT.
 *
 * O token e auto-contido e assinado (HMAC-SHA): o servidor nao guarda sessao,
 * apenas confere a assinatura a cada requisicao. Isso mantem a API stateless,
 * que e o que permite escalar horizontalmente sem sessao compartilhada.
 */
@Service
public class JwtService {

    private final SecretKey chave;
    private final long expiracaoMinutos;

    public JwtService(
            @Value("${sigoh.jwt.secret}") String secret,
            @Value("${sigoh.jwt.expiration-minutes}") long expiracaoMinutos
    ) {
        this.chave = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expiracaoMinutos = expiracaoMinutos;
    }

    public String gerarToken(String email, Map<String, Object> claimsExtras) {
        Instant agora = Instant.now();
        return Jwts.builder()
                .claims(claimsExtras)
                .subject(email)
                .issuedAt(Date.from(agora))
                .expiration(Date.from(agora.plus(expiracaoMinutos, ChronoUnit.MINUTES)))
                .signWith(chave)
                .compact();
    }

    public String extrairEmail(String token) {
        return extrairClaim(token, Claims::getSubject);
    }

    public boolean tokenValido(String token, String emailEsperado) {
        return emailEsperado.equals(extrairEmail(token)) && !expirado(token);
    }

    private boolean expirado(String token) {
        return extrairClaim(token, Claims::getExpiration).before(new Date());
    }

    private <T> T extrairClaim(String token, Function<Claims, T> resolver) {
        Claims claims = Jwts.parser().verifyWith(chave).build().parseSignedClaims(token).getPayload();
        return resolver.apply(claims);
    }
}
