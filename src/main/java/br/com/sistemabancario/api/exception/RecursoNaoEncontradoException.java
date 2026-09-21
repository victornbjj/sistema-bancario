package br.com.sistemabancario.api.exception;

import org.springframework.http.HttpStatus;

public class RecursoNaoEncontradoException extends  NegocioException{
    
   private static final long serialVersionUID = 1L;
   

     public RecursoNaoEncontradoException(String message) {
        super(HttpStatus.NOT_FOUND, "Recurso não encontrado", message);
    }
 
    public static RecursoNaoEncontradoException correntista(Long id) {
        return new RecursoNaoEncontradoException(
                String.format("Correntista com id %d não encontrado", id));
    }
 
    public static RecursoNaoEncontradoException conta(Long id) {
        return new RecursoNaoEncontradoException(
                String.format("Conta com id %d não encontrada", id));
    } 



}
