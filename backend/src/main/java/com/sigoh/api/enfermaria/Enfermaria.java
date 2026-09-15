package com.sigoh.api.enfermaria;

import com.sigoh.api.bloco.Bloco;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Subdivisao de um bloco. Agrupa leitos. */
@Entity
@Table(name = "enfermarias")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Enfermaria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nome;

    // LAZY: o bloco so e carregado do banco quando realmente acessado,
    // evitando consultas desnecessarias ao listar enfermarias.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "bloco_id", nullable = false)
    private Bloco bloco;

    @Column(nullable = false)
    @Builder.Default
    private boolean ativo = true;
}
