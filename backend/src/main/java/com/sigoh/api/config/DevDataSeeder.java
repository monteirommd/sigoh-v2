package com.sigoh.api.config;

import com.sigoh.api.bloco.Bloco;
import com.sigoh.api.bloco.BlocoRepository;
import com.sigoh.api.common.enums.Role;
import com.sigoh.api.common.enums.StatusLeito;
import com.sigoh.api.enfermaria.Enfermaria;
import com.sigoh.api.enfermaria.EnfermariaRepository;
import com.sigoh.api.leito.Leito;
import com.sigoh.api.leito.LeitoRepository;
import com.sigoh.api.usuario.Usuario;
import com.sigoh.api.usuario.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Carga de dados de demonstracao para desenvolvimento.
 *
 * Fica em codigo Java (e nao numa migration Flyway) porque as senhas precisam
 * ser processadas pelo mesmo PasswordEncoder da aplicacao - um hash BCrypt
 * escrito a mao num arquivo .sql ficaria preso a um custo/salt fixo e poderia
 * divergir da configuracao real. Migrations cuidam do schema; dados de teste
 * sao responsabilidade do ambiente.
 *
 * Ativo apenas quando sigoh.seed.enabled=true (perfil dev).
 */
@Component
@ConditionalOnProperty(name = "sigoh.seed.enabled", havingValue = "true")
public class DevDataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DevDataSeeder.class);

    private final UsuarioRepository usuarioRepository;
    private final BlocoRepository blocoRepository;
    private final EnfermariaRepository enfermariaRepository;
    private final LeitoRepository leitoRepository;
    private final PasswordEncoder passwordEncoder;

    public DevDataSeeder(
            UsuarioRepository usuarioRepository,
            BlocoRepository blocoRepository,
            EnfermariaRepository enfermariaRepository,
            LeitoRepository leitoRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.usuarioRepository = usuarioRepository;
        this.blocoRepository = blocoRepository;
        this.enfermariaRepository = enfermariaRepository;
        this.leitoRepository = leitoRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (usuarioRepository.count() > 0) {
            return; // ja semeado
        }

        usuarioRepository.save(Usuario.builder()
                .nome("Administrador")
                .email("admin@sigoh.com")
                .senhaHash(passwordEncoder.encode("admin123"))
                .role(Role.ADMIN)
                .build());

        usuarioRepository.save(Usuario.builder()
                .nome("Usuario Comum")
                .email("user@sigoh.com")
                .senhaHash(passwordEncoder.encode("user123"))
                .role(Role.USER)
                .build());

        Bloco blocoA = blocoRepository.save(Bloco.builder().nome("Bloco A").build());
        Bloco blocoB = blocoRepository.save(Bloco.builder().nome("Bloco B").build());

        Enfermaria clinicaMedica = enfermariaRepository.save(
                Enfermaria.builder().nome("Clinica Medica").bloco(blocoA).build());
        Enfermaria pediatria = enfermariaRepository.save(
                Enfermaria.builder().nome("Pediatria").bloco(blocoA).build());
        Enfermaria uti = enfermariaRepository.save(
                Enfermaria.builder().nome("UTI Adulto").bloco(blocoB).build());

        criarLeitos(clinicaMedica, "CM", List.of(
                StatusLeito.DISPONIVEL, StatusLeito.OCUPADO, StatusLeito.OCUPADO, StatusLeito.LIMPEZA));
        criarLeitos(pediatria, "PED", List.of(
                StatusLeito.DISPONIVEL, StatusLeito.DISPONIVEL, StatusLeito.OCUPADO));
        criarLeitos(uti, "UTI", List.of(
                StatusLeito.OCUPADO, StatusLeito.OCUPADO, StatusLeito.BLOQUEADO, StatusLeito.DISPONIVEL));

        log.info("Dados de demonstracao carregados (perfil dev)");
    }

    private void criarLeitos(Enfermaria enfermaria, String prefixo, List<StatusLeito> status) {
        for (int i = 0; i < status.size(); i++) {
            leitoRepository.save(Leito.builder()
                    .codigo(String.format("%s-%02d", prefixo, i + 1))
                    .enfermaria(enfermaria)
                    .status(status.get(i))
                    .possuiOxigenio(i % 2 == 0)
                    .build());
        }
    }
}
