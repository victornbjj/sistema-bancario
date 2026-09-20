package br.com.sistemabancario.api.exception;

public class BusinessException extends RuntimeException{
    public BusinessException(String mensage){
        super(mensage);
    }
}
