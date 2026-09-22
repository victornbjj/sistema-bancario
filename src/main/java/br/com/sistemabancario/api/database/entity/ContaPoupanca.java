package br.com.sistemabancario.api.database.entity;

import java.math.BigDecimal;
import java.math.RoundingMode;

import javax.persistence.Entity;
import javax.persistence.Table;
import br.com.sistemabancario.api.enums.TipoTransacao;
import br.com.sistemabancario.api.exception.SaldoInsuficienteException;
import br.com.sistemabancario.api.exception.ValorInvalidoException;




@Entity 
@Table (name = "conta_poupanca")
public class ContaPoupanca extends ContaEntity{




    @Override
    public TransacaoEntity sacar(BigDecimal valor) {
        if(valor == null|| valor.compareTo(BigDecimal.ZERO) <= 0){
        throw new ValorInvalidoException("Valor do saque deve ser maior que zero");
       } 
        
       BigDecimal dispinivel = getSaldo();

        if (valor.compareTo(getSaldo()) > 0){
            throw SaldoInsuficienteException.paraSaque(valor,dispinivel);
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
       if(valor == null || valor.compareTo(BigDecimal.ZERO) <= 0){
        throw new ValorInvalidoException("Valor depositado não deve ser nullo ouser menor que zero");
       }
       
       this.adicionarValor(valor);

       return TransacaoEntity.builder()
               .tipo(TipoTransacao.DEPOSITO)
               .valor(valor)
               .conta(this)
               .build();
        
    }


    public TransacaoEntity aplicarRendimentoMensal(BigDecimal taxa){
        if(taxa == null || taxa.compareTo(BigDecimal.ZERO) <= 0 || taxa.compareTo(BigDecimal.ONE) > 0 ){
            throw ValorInvalidoException.taxaForaDoIntervalo("taxa");
        }
       
        if(this.getSaldo().compareTo(BigDecimal.ZERO) <= 0){
            throw new  SaldoInsuficienteException("Rendimento exige salado positivo");
        }
       
        BigDecimal rendimento  = this.getSaldo()
        .multiply(taxa)
        .setScale(2, RoundingMode.HALF_EVEN);

        this.adicionarValor(rendimento);

        return  TransacaoEntity.builder()
                .tipo(TipoTransacao.RENDIMENTO)
                .valor(rendimento)
                .conta(this)
                .build();
       
    }
    
}
