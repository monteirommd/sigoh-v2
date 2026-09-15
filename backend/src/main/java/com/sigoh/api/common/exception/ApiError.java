package com.sigoh.api.common.exception;

import java.time.Instant;

/** Formato unico de erro devolvido pela API, para o front-end tratar sempre igual. */
public record ApiError(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path
) {
}
