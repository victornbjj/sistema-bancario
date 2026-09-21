package br.com.sistemabancario.api.exception;

import java.math.BigDecimal;

import org.springframework.http.HttpStatus;


public class SaldoInsuficienteException extends NegocioException{
    
   private static final long serialVersionUID = 1L;
   

   public SaldoInsuficienteException(String message){
    super(HttpStatus.UNPROCESSABLE_ENTITY, "Saldo insuficente", message);
   }


   public SaldoInsuficienteException paraSaque(BigDecimal valorSolicitado, BigDecimal valorDisponivel){
    return new  SaldoInsuficienteException(String.format(
        "Saque de R$ %.2f excede o valor disponível de R$ %.2f",valorSolicitado, valorDisponivel));
   }



}
