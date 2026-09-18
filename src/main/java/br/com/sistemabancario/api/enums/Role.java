package br.com.sistemabancario.api.enums;

public enum Role {
   
   ADMIN("admin"),

   CORRENTISTA("correntista");
    

   String role; 
   
   
   Role(String role){
    this.role = role;
   }

   public String getRole() {
       return role;
   }

}
