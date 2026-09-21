package br.com.sistemabancario.api.exception;

import org.springframework.http.HttpStatus;

public abstract class NegocioException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final HttpStatus status;
    private final String error;

    protected NegocioException(HttpStatus status, String error, String message) {
        super(message);
        this.status = status;
        this.error = error;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getError() {
        return error;
    }
}