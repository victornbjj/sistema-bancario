package br.com.sistemabancario.api.exception;

import org.springframework.http.HttpStatus;



public class TipoContaInvalidoException extends NegocioException{

   private static final long serialVersionUID = 1L;
    
   public TipoContaInvalidoException(String message){
    super(HttpStatus.UNPROCESSABLE_ENTITY, "Operção invalida com tipo de conta", message);
   }


   public static TipoContaInvalidoException rendimentoExigePoupanca(){
     return  new TipoContaInvalidoException("Rendimento só pode ser aplicado em Conta Poupança");
   }


   public static  TipoContaInvalidoException jurosExigeCorrente(){
    return new TipoContaInvalidoException("Juros só poder ser aplicado em conta corrente");
   }

   public static  TipoContaInvalidoException rendimentoExigeSaldo(){
    return new TipoContaInvalidoException("Para rendimento exige saldo positivo");
   }

   public  static  TipoContaInvalidoException jurosExigeSaldoNegativo(){
    return new  TipoContaInvalidoException("Para aplicação de juro e preciso estar com saldo negativo");
   }
    
}
