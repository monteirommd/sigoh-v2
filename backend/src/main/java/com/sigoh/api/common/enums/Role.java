package com.sigoh.api.common.enums;

/**
 * Perfis de acesso do sistema (RBAC).
 *
 * USER  - consulta o mapa de leitos e a ocupacao; nao altera nada.
 * ADMIN - alem da consulta, administra a estrutura (blocos, enfermarias,
 *         leitos) e atualiza o status operacional dos leitos.
 *
 * O Spring Security adiciona o prefixo "ROLE_" automaticamente ao usar
 * hasRole("ADMIN"), por isso o enum guarda apenas "ADMIN".
 */
public enum Role {
    USER,
    ADMIN
}
