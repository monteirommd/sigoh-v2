package com.sigoh.api.common.exception;

/**
 * Violacao de regra de negocio (ex.: excluir um bloco que ainda possui
 * enfermarias). Traduzida para HTTP 422.
 */
public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
}
