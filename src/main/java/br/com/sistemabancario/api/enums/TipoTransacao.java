package br.com.sistemabancario.api.enums;

public enum TipoTransacao {
   DEPOSITO("deposito"),
   SAQUE("saque"),
   RENDIMENTO("rendimento"),
   JUROS("juros");
   
   
   private String tipoTransacao;
   

   TipoTransacao (String tipoTransacao){
     this.tipoTransacao = tipoTransacao;
   }
    
   public String getTipoTransacao() {
       return tipoTransacao;
   }


}
