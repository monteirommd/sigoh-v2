package com.sigoh.api.leito;

import com.sigoh.api.common.enums.StatusLeito;
import com.sigoh.api.enfermaria.Enfermaria;
import com.sigoh.api.usuario.Usuario;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/** Entidade central do dominio: a unidade cuja ocupacao o sistema controla. */
@Entity
@Table(name = "leitos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Leito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Identificacao usada pela equipe no dia a dia (ex.: "A1-03"). */
    @Column(nullable = false, length = 30)
    private String codigo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "enfermaria_id", nullable = false)
    private Enfermaria enfermaria;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusLeito status;

    /** Caracteristicas que determinam a que tipo de paciente o leito atende. */
    @Column(name = "possui_oxigenio", nullable = false)
    @Builder.Default
    private boolean possuiOxigenio = false;

    @Column(nullable = false)
    @Builder.Default
    private boolean isolamento = false;

    @Column(nullable = false)
    @Builder.Default
    private boolean ativo = true;

    @Column(name = "atualizado_em", nullable = false)
    @Builder.Default
    private Instant atualizadoEm = Instant.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "atualizado_por")
    private Usuario atualizadoPor;
}
