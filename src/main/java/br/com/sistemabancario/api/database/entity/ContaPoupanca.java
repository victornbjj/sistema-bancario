package br.com.sistemabancario.api.database.entity;

import java.math.BigDecimal;

import javax.persistence.Entity;
import javax.persistence.Table;




@Entity 
@Table (name = "conta_poupanca")
public class ContaPoupanca extends ContaEntity{


    @Override
    public Boolean sacar(BigDecimal valor) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'sacar'");
    }

    @Override
    public void depositar(BigDecimal valor) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'depositar'");
    }
    
}
