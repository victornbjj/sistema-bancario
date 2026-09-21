package br.com.sistemabancario.api.exception;

import org.springframework.http.HttpStatus;



public class RegistroDuplicadoException extends NegocioException{

   private static final long serialVersionUID = 1L;

   public RegistroDuplicadoException(String message){
    super(HttpStatus.CONFLICT, "Registro Duplicado", message);
   }

   public static RegistroDuplicadoException documento(String documento){
     return new RegistroDuplicadoException(String.format(
        "Já existe um correntista cadastro com esse documento '%s'", documento));
   }


   public static RegistroDuplicadoException email(String email){
    return  new RegistroDuplicadoException(String.format
        ("Já existe um correntista cadastrado com o e-mail '%s'", email));
   }


   public static RegistroDuplicadoException telefone(String telefone){
     return new RegistroDuplicadoException(String.format(
        "Já existe um correntista cadastrado com esse telefone '%s'", telefone));
   }


}
