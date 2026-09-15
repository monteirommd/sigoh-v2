package com.sigoh.api.ocupacao.dto;

/**
 * Ocupacao consolidada de um bloco no momento da consulta.
 * A taxa e calculada no servidor para que todas as telas usem exatamente
 * a mesma formula.
 */
public record OcupacaoBlocoDTO(
        Long blocoId,
        String blocoNome,
        int totalLeitos,
        int ocupados,
        int disponiveis,
        int emLimpeza,
        int bloqueados,
        double taxaOcupacao
) {
}
