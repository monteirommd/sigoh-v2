package com.sigoh.api.common.exception;

/** Recurso solicitado nao existe. Traduzida para HTTP 404. */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String recurso, Object id) {
        super(recurso + " nao encontrado(a): " + id);
    }
}
