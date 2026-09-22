package br.com.sistemabancario.api.database.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.com.sistemabancario.api.enums.TipoTransacao;
import br.com.sistemabancario.api.exception.SaldoInsuficienteException;
import br.com.sistemabancario.api.exception.ValorInvalidoException;

/**
 * Marco 2 — Dominio financeiro (ContaPoupanca).
 * Cobre deposito, saque limitado ao saldo (sem limite/cheque especial),
 * rendimento com a formula saldo + saldo*taxa, e a garantia de que
 * nenhuma operacao invalida altera saldo ou cria transacao.
 */
class ContaPoupancaTest {

    private ContaPoupanca conta;

    @BeforeEach
    void setUp() {
        conta = new ContaPoupanca();
    }

    @Test
    @DisplayName("Deposito positivo aumenta o saldo e cria transacao DEPOSITO")
    void deveAumentarSaldoAoDepositarValorPositivo() {
        TransacaoEntity transacao = conta.depositar(new BigDecimal("200.00"));

        assertThat(conta.getSaldo()).isEqualByComparingTo("200.00");
        assertThat(transacao.getTipo()).isEqualTo(TipoTransacao.DEPOSITO);
        assertThat(transacao.getValor()).isEqualByComparingTo("200.00");
    }

    @Test
    @DisplayName("Deposito com valor zero ou negativo lanca excecao e nao altera saldo")
    void deveRejeitarDepositoComValorZeroOuNegativo() {
        assertThatThrownBy(() -> conta.depositar(BigDecimal.ZERO))
                .isInstanceOf(ValorInvalidoException.class);
        assertThatThrownBy(() -> conta.depositar(new BigDecimal("-5.00")))
                .isInstanceOf(ValorInvalidoException.class);
        assertThatThrownBy(() -> conta.depositar(null))
                .isInstanceOf(ValorInvalidoException.class);

        assertThat(conta.getSaldo()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("Saque ate o valor do saldo e permitido")
    void devePermitirSaqueAteOSaldo() {
        conta.depositar(new BigDecimal("300.00"));

        TransacaoEntity transacao = conta.sacar(new BigDecimal("300.00"));

        assertThat(conta.getSaldo()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(transacao.getTipo()).isEqualTo(TipoTransacao.SAQUE);
    }

    @Test
    @DisplayName("Saque acima do saldo lanca SaldoInsuficienteException, mesmo sem limite envolvido")
    void deveRejeitarSaqueAcimaDoSaldo() {
        conta.depositar(new BigDecimal("100.00"));

        assertThatThrownBy(() -> conta.sacar(new BigDecimal("100.01")))
                .isInstanceOf(SaldoInsuficienteException.class);

        assertThat(conta.getSaldo()).isEqualByComparingTo("100.00");
    }

    @Test
    @DisplayName("Saque com valor invalido (zero, negativo ou nulo) lanca ValorInvalidoException")
    void deveRejeitarSaqueComValorInvalido() {
        conta.depositar(new BigDecimal("50.00"));

        assertThatThrownBy(() -> conta.sacar(BigDecimal.ZERO))
                .isInstanceOf(ValorInvalidoException.class);
        assertThatThrownBy(() -> conta.sacar(new BigDecimal("-1.00")))
                .isInstanceOf(ValorInvalidoException.class);
        assertThatThrownBy(() -> conta.sacar(null))
                .isInstanceOf(ValorInvalidoException.class);

        assertThat(conta.getSaldo()).isEqualByComparingTo("50.00");
    }

    @Test
    @DisplayName("Rendimento usa a formula saldo + saldo*taxa e cria transacao RENDIMENTO")
    void deveAplicarRendimentoUsandoSaldoMaisSaldoVezesTaxa() {
        conta.depositar(new BigDecimal("1000.00"));

        TransacaoEntity transacao = conta.aplicarRendimentoMensal(new BigDecimal("0.005"));

        // rendimento = 1000 * 0.005 = 5.00 ; saldo = 1000 + 5 = 1005.00
        assertThat(transacao.getTipo()).isEqualTo(TipoTransacao.RENDIMENTO);
        assertThat(transacao.getValor()).isEqualByComparingTo("5.00");
        assertThat(conta.getSaldo()).isEqualByComparingTo("1005.00");
    }

    @Test
    @DisplayName("Rendimento com saldo zero ou negativo lanca SaldoInsuficienteException e nao altera saldo")
    void deveRejeitarRendimentoComSaldoNaoPositivo() {
        assertThatThrownBy(() -> conta.aplicarRendimentoMensal(new BigDecimal("0.005")))
                .isInstanceOf(SaldoInsuficienteException.class);

        assertThat(conta.getSaldo()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("Rendimento com taxa fora do intervalo (0, 1] lanca ValorInvalidoException")
    void deveRejeitarRendimentoComTaxaForaDoIntervalo() {
        conta.depositar(new BigDecimal("100.00"));

        assertThatThrownBy(() -> conta.aplicarRendimentoMensal(BigDecimal.ZERO))
                .isInstanceOf(ValorInvalidoException.class);
        assertThatThrownBy(() -> conta.aplicarRendimentoMensal(new BigDecimal("1.01")))
                .isInstanceOf(ValorInvalidoException.class);
        assertThatThrownBy(() -> conta.aplicarRendimentoMensal(null))
                .isInstanceOf(ValorInvalidoException.class);

        // saldo permanece intacto apos as tentativas invalidas
        assertThat(conta.getSaldo()).isEqualByComparingTo("100.00");
    }

    @Test
    @DisplayName("Arredondamento do rendimento usa escala 2 e RoundingMode.HALF_EVEN")
    void deveArredondarRendimentoComHalfEven() {
        conta.depositar(new BigDecimal("333.33"));

        // 333.33 * 0.015 = 4.99995 -> HALF_EVEN escala 2 -> 5.00
        TransacaoEntity transacao = conta.aplicarRendimentoMensal(new BigDecimal("0.015"));

        assertThat(transacao.getValor()).isEqualByComparingTo("5.00");
        assertThat(transacao.getValor().scale()).isEqualTo(2);
    }
}
