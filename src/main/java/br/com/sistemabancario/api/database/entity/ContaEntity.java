package br.com.sistemabancario.api.database.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import javax.persistence.Table;

import br.com.sistemabancario.api.enums.TipoConta;
import lombok.AllArgsConstructor;

import lombok.NoArgsConstructor;

@Entity
@Table(name = "conta")
@AllArgsConstructor
@NoArgsConstructor
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class ContaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String numero;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal saldo = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoConta tipo;

    @Column(nullable = false, name = "data_abertura")
    private LocalDateTime dataAbertura;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "correntista_id", nullable = false)
    private CorrentistaEntity correntista;

    @PrePersist
    public void prepararData() {
        if (dataAbertura == null) {
            dataAbertura = LocalDateTime.now();
        }
    }

    public abstract TransacaoEntity sacar(BigDecimal valor);

    public abstract TransacaoEntity depositar(BigDecimal valor);

    protected void diminuirSaldo(BigDecimal valor){
        saldo = saldo.subtract(valor);
    }

    protected  void adicionarValor(BigDecimal valor){
        saldo = saldo.add(valor);
    }

    public Long getId() {
        return id;
    }

    public String getNumero() {
        return numero;
    }

    public BigDecimal getSaldo() {
        return saldo;
    }

    public TipoConta getTipo() {
        return tipo;
    }

    public LocalDateTime getDataAbertura() {
        return dataAbertura;
    }

    public CorrentistaEntity getCorrentista() {
        return correntista;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }


    public void setTipo(TipoConta tipo) {
        this.tipo = tipo;
    }

    public void setDataAbertura(LocalDateTime dataAbertura) {
        this.dataAbertura = dataAbertura;
    }

    public void setCorrentista(CorrentistaEntity correntista) {
        this.correntista = correntista;
    }



};
