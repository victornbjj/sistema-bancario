package br.com.sistemabancario.api.enums;

public enum Role {
   
   ADMIN("admin"),
   OPERADOR("operador");
    

   String role; 
   
   
   Role(String role){
    this.role = role;
   }

   public String getRole() {
       return role;
   }

}
