package br.com.sistemabancario.api.exception;

import org.springframework.http.HttpStatus;

public class PeriodoMinimoNaoAtingidoException extends NegocioException{
    
    private static final long serialVersionUID = 1L;

    public PeriodoMinimoNaoAtingidoException(String messege){
        super(HttpStatus.UNPROCESSABLE_ENTITY,"Período mínimo não atingido", messege);
    
    }

    public static PeriodoMinimoNaoAtingidoException paraOperacao(String operacao, long diasRestantes) {
        return new PeriodoMinimoNaoAtingidoException(
                String.format("%s só pode ser aplicado a cada 30 dias. Faltam %d dia(s).", operacao, diasRestantes));
    }
}
