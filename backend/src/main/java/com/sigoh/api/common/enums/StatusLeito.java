package com.sigoh.api.common.enums;

/**
 * Estado operacional de um leito.
 *
 * O ciclo normal de um leito e DISPONIVEL -> OCUPADO -> LIMPEZA -> DISPONIVEL.
 * BLOQUEADO e um estado administrativo (manutencao, interdicao) que sai do
 * ciclo e so retorna por acao explicita.
 */
public enum StatusLeito {
    DISPONIVEL,
    OCUPADO,
    LIMPEZA,
    BLOQUEADO;

    /** Proximo status no ciclo operacional, usado pela acao rapida de ronda. */
    public StatusLeito proximo() {
        return switch (this) {
            case DISPONIVEL -> OCUPADO;
            case OCUPADO -> LIMPEZA;
            case LIMPEZA -> DISPONIVEL;
            case BLOQUEADO -> BLOQUEADO;
        };
    }
}
