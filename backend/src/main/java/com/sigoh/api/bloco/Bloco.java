package com.sigoh.api.bloco;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Maior unidade da estrutura fisica do hospital. Agrupa enfermarias. */
@Entity
@Table(name = "blocos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Bloco {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nome;

    @Column(nullable = false)
    @Builder.Default
    private boolean ativo = true;
}
