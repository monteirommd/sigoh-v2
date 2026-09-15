package com.sigoh.api.historico;

import com.sigoh.api.common.enums.StatusLeito;
import com.sigoh.api.leito.Leito;
import com.sigoh.api.usuario.Usuario;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Registro imutavel de uma mudanca de status.
 *
 * Nao ha setters nem rotina de atualizacao/remocao: a tabela e append-only por
 * decisao de projeto, para servir como trilha de auditoria confiavel (quem
 * mudou o que e quando).
 */
@Entity
@Table(name = "historico_leitos")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HistoricoLeito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "leito_id", nullable = false)
    private Leito leito;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_anterior", nullable = false, length = 20)
    private StatusLeito statusAnterior;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_novo", nullable = false, length = 20)
    private StatusLeito statusNovo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "registrado_em", nullable = false)
    private Instant registradoEm;
}
