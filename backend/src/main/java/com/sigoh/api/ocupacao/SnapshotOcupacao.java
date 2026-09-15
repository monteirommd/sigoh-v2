package com.sigoh.api.ocupacao;

import com.sigoh.api.bloco.Bloco;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/** Fotografia diaria da ocupacao de um bloco, base dos relatorios historicos. */
@Entity
@Table(name = "snapshots_ocupacao")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SnapshotOcupacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "data_registro", nullable = false)
    private LocalDate dataRegistro;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "bloco_id", nullable = false)
    private Bloco bloco;

    @Column(name = "total_leitos", nullable = false)
    private int totalLeitos;

    @Column(nullable = false)
    private int ocupados;

    @Column(nullable = false)
    private int disponiveis;

    @Column(name = "em_limpeza", nullable = false)
    private int emLimpeza;

    @Column(nullable = false)
    private int bloqueados;
}
