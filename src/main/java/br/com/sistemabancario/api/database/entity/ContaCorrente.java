package br.com.sistemabancario.api.database.entity;

import java.math.BigDecimal;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Entity 
@Table(name ="conta_corrente")
@AllArgsConstructor 
@NoArgsConstructor 
public class ContaCorrente extends ContaEntity{



    
     @Column (nullable = false  ,precision = 15, scale = 2)
    private BigDecimal limite = BigDecimal.ZERO;

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
