package br.com.sistemabancario.api.enums;

public enum TipoConta {
    CORRENTE("corrente"), 
    POUPANCA("poupanca");

    
    String TipoConta; 


    TipoConta(String tipoConta){
      this.TipoConta = tipoConta;
    }
    
    public String getTipoConta() {
        return TipoConta;
    }

    
}
