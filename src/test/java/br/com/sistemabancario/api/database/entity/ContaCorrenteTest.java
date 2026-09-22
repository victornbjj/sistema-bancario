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
 * Marco 2 — Dominio financeiro (ContaCorrente).
 * Cobre as regras do roadmap: deposito, saque dentro de saldo+limite,
 * saque acima do limite, juros em saldo negativo, taxa fora do intervalo,
 * e a garantia de que nenhuma operacao invalida altera saldo ou cria transacao.
 */
class ContaCorrenteTest {

    private ContaCorrente conta;

    @BeforeEach
    void setUp() {
        conta = new ContaCorrente();
        conta.setLimite(new BigDecimal("500.00"));
    }

    @Test
    @DisplayName("Deposito positivo aumenta o saldo e cria transacao DEPOSITO")
    void deveAumentarSaldoAoDepositarValorPositivo() {
        TransacaoEntity transacao = conta.depositar(new BigDecimal("100.00"));

        assertThat(conta.getSaldo()).isEqualByComparingTo("100.00");
        assertThat(transacao.getTipo()).isEqualTo(TipoTransacao.DEPOSITO);
        assertThat(transacao.getValor()).isEqualByComparingTo("100.00");
        assertThat(transacao.getConta()).isSameAs(conta);
    }

    @Test
    @DisplayName("Deposito com valor zero ou negativo lanca excecao e nao altera saldo")
    void deveRejeitarDepositoComValorZeroOuNegativo() {
        assertThatThrownBy(() -> conta.depositar(BigDecimal.ZERO))
                .isInstanceOf(ValorInvalidoException.class);
        assertThatThrownBy(() -> conta.depositar(new BigDecimal("-10.00")))
                .isInstanceOf(ValorInvalidoException.class);
        assertThatThrownBy(() -> conta.depositar(null))
                .isInstanceOf(ValorInvalidoException.class);

        assertThat(conta.getSaldo()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("Saque dentro do saldo mais limite e permitido, mesmo deixando saldo negativo")
    void devePermitirSaqueDentroDoSaldoMaisLimite() {
        conta.depositar(new BigDecimal("100.00"));

        TransacaoEntity transacao = conta.sacar(new BigDecimal("400.00"));

        assertThat(conta.getSaldo()).isEqualByComparingTo("-300.00");
        assertThat(transacao.getTipo()).isEqualTo(TipoTransacao.SAQUE);
        assertThat(transacao.getValor()).isEqualByComparingTo("400.00");
    }

    @Test
    @DisplayName("Saque acima de saldo mais limite lanca SaldoInsuficienteException e nao altera saldo")
    void deveRejeitarSaqueAcimaDoSaldoMaisLimite() {
        conta.depositar(new BigDecimal("100.00")); // disponivel = 100 + 500 = 600

        assertThatThrownBy(() -> conta.sacar(new BigDecimal("600.01")))
                .isInstanceOf(SaldoInsuficienteException.class);

        assertThat(conta.getSaldo()).isEqualByComparingTo("100.00");
    }

    @Test
    @DisplayName("Saque com valor zero, negativo ou nulo lanca ValorInvalidoException")
    void deveRejeitarSaqueComValorInvalido() {
        assertThatThrownBy(() -> conta.sacar(BigDecimal.ZERO))
                .isInstanceOf(ValorInvalidoException.class);
        assertThatThrownBy(() -> conta.sacar(new BigDecimal("-1.00")))
                .isInstanceOf(ValorInvalidoException.class);
        assertThatThrownBy(() -> conta.sacar(null))
                .isInstanceOf(ValorInvalidoException.class);
    }

    @Test
    @DisplayName("Juros em saldo negativo aumenta a divida pelo valor exato (|saldo| * taxa)")
    void deveAumentarDividaAoAplicarJurosComSaldoNegativo() {
        conta.depositar(new BigDecimal("100.00"));
        conta.sacar(new BigDecimal("400.00")); // saldo = -300.00

        TransacaoEntity transacao = conta.aplicarJuros(new BigDecimal("0.02"));

        // juros = |−300| * 0.02 = 6.00 ; saldo = -300 - 6 = -306.00
        assertThat(transacao.getTipo()).isEqualTo(TipoTransacao.JUROS);
        assertThat(transacao.getValor()).isEqualByComparingTo("6.00");
        assertThat(conta.getSaldo()).isEqualByComparingTo("-306.00");
    }

    @Test
    @DisplayName("Juros em saldo positivo ou zero lanca SaldoInsuficienteException e nao altera saldo")
    void deveRejeitarJurosComSaldoNaoNegativo() {
        conta.depositar(new BigDecimal("50.00"));

        assertThatThrownBy(() -> conta.aplicarJuros(new BigDecimal("0.02")))
                .isInstanceOf(SaldoInsuficienteException.class);

        assertThat(conta.getSaldo()).isEqualByComparingTo("50.00");
    }

    @Test
    @DisplayName("Juros com taxa fora do intervalo (0, 1] lanca ValorInvalidoException")
    void deveRejeitarJurosComTaxaForaDoIntervalo() {
        conta.sacar(new BigDecimal("100.00")); // saldo = -100.00 (usa limite)

        assertThatThrownBy(() -> conta.aplicarJuros(BigDecimal.ZERO))
                .isInstanceOf(ValorInvalidoException.class);
        assertThatThrownBy(() -> conta.aplicarJuros(new BigDecimal("1.01")))
                .isInstanceOf(ValorInvalidoException.class);
        assertThatThrownBy(() -> conta.aplicarJuros(null))
                .isInstanceOf(ValorInvalidoException.class);

        // nenhuma dessas tentativas deve ter alterado o saldo original
        assertThat(conta.getSaldo()).isEqualByComparingTo("-100.00");
    }
}
