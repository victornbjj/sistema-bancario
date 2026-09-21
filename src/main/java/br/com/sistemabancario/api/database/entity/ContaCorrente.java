package br.com.sistemabancario.api.database.entity;

import java.math.BigDecimal;
import java.math.RoundingMode;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import br.com.sistemabancario.api.enums.TipoTransacao;
import br.com.sistemabancario.api.exception.SaldoInsuficienteException;
import br.com.sistemabancario.api.exception.ValorInvalidoException;
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
    public TransacaoEntity sacar(BigDecimal valor) {
       if(valor == null|| valor.compareTo(BigDecimal.ZERO) <= 0){
        throw new ValorInvalidoException("Valor do saque deve ser maior que zero");
       } 

       BigDecimal disponivel = getSaldo().add(limite);
       
        if (valor.compareTo(disponivel) > 0){
        throw SaldoInsuficienteException.paraSaque(
            valor,
            disponivel);
        }
          
       this.diminuirSaldo(valor);
       return TransacaoEntity.builder()
               .tipo(TipoTransacao.SAQUE)
               .valor(valor)
               .conta(this)
               .build();
       

    }

    @Override
    public TransacaoEntity depositar(BigDecimal valor) {
       if(valor == null || valor.compareTo(BigDecimal.ZERO) < 0){
        throw new ValorInvalidoException("Valor depositado não deve ser nullo ouser menor que zero");
       }
       
       this.adicionarValor(valor);

       return TransacaoEntity.builder()
               .tipo(TipoTransacao.DEPOSITO)
               .valor(valor)
               .conta(this)
               .build();
        
    }
      

    public TransacaoEntity aplicarJuros(BigDecimal taxa) {
    if (taxa == null || taxa.compareTo(BigDecimal.ZERO) <= 0 || taxa.compareTo(BigDecimal.ONE) > 0) {
        throw ValorInvalidoException.taxaForaDoIntervalo("taxa");
    }

    if (getSaldo().compareTo(BigDecimal.ZERO) >= 0) {
        throw new SaldoInsuficienteException("Juros exige saldo negativo");
    }

    BigDecimal valorJuros = getSaldo()
            .abs()                      
            .multiply(taxa)
            .setScale(2, RoundingMode.HALF_EVEN);

    diminuirSaldo(valorJuros); 

    return TransacaoEntity.builder()
            .tipo(TipoTransacao.JUROS)
            .valor(valorJuros)
            .conta(this)
            .build();
}


    public BigDecimal getLimite(){
        return  limite;
    }

    public void setLimite(BigDecimal limite) {
        this.limite = limite;
    }

    

    
}
