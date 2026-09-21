package br.com.sistemabancario.api.exception;

import org.springframework.http.HttpStatus;

public class ValorInvalidoException extends NegocioException {

    
    public ValorInvalidoException(String message) {
        super(HttpStatus.BAD_REQUEST, "Valor inválido", message);
    }

      public static ValorInvalidoException taxaForaDoIntervalo(String campo) {
        return new ValorInvalidoException(
                String.format("O campo '%s' deve estar entre 0 (exclusive) e 1 (inclusive)", campo));
    }

}
