package br.com.sistemabancario.api.exception;

import org.springframework.http.HttpStatus;



public class RequisicaoInvalidaException extends NegocioException {
    
    
    private static final long serialVersionUID = 1L;

     public RequisicaoInvalidaException(String message) {
        super(HttpStatus.BAD_REQUEST, "Requisição inválida", message);
    }

     public static RequisicaoInvalidaException limiteNaoPermitidoParaPoupanca() {
        return new RequisicaoInvalidaException(
                "Conta Poupança não aceita o campo 'limite'");
    }

     public static RequisicaoInvalidaException limiteNegativo() {
        return new RequisicaoInvalidaException(
                "O campo 'limite' não pode ser negativo");
    }

     public static RequisicaoInvalidaException periodoInvalido() {
        return new RequisicaoInvalidaException(
                "O parâmetro 'dataInicial' não pode ser posterior a 'dataFinal'");
    }


    


}