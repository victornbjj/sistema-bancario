package br.com.sistemabancario.api.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import br.com.sistemabancario.api.dto.ContaResponse;
import br.com.sistemabancario.api.dto.TransacaoResponse;
import br.com.sistemabancario.api.enums.TipoConta;
import br.com.sistemabancario.api.enums.TipoTransacao;
import br.com.sistemabancario.api.exception.PeriodoMinimoNaoAtingidoException;
import br.com.sistemabancario.api.exception.RecursoNaoEncontradoException;
import br.com.sistemabancario.api.exception.RequisicaoInvalidaException;
import br.com.sistemabancario.api.exception.SaldoInsuficienteException;
import br.com.sistemabancario.api.exception.TipoContaInvalidoException;
import br.com.sistemabancario.api.service.ContaService;
import br.com.sistemabancario.api.service.TransacaoService;

/**
 * Marco 3 (correntistas e contas), Marco 4 (deposito/saque) e Marco 5 (rendimento/juros)
 * -- testes de contrato do ContaController via MockMvc, com ContaService e
 * TransacaoService mockados (arquitetura atual, apos a divisao dos services).
 *
 * ATENCAO: o teste "deveRetornar400QuandoPoupancaInformaLimite" espera o status
 * que o roadmap define para esse erro (400). No ContaService atual esse caso
 * lanca TipoContaInvalidoException (422), entao esse teste falha ate a correcao
 * ser feita (trocar para RequisicaoInvalidaException.limiteNaoPermitidoParaPoupanca()).
 */
@WebMvcTest(ContaController.class)
class ContaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ContaService contaService;

    @MockBean
    private TransacaoService transacaoService;

    // ---------- POST /api/v1/contas ----------

    @Test
    @DisplayName("Deve criar conta corrente valida e retornar 201 com Location")
    void deveCriarContaCorrenteERetornarLocation() throws Exception {
        ContaResponse response = ContaResponse.builder()
                .id(1L)
                .numero("00000001")
                .saldo(BigDecimal.ZERO)
                .tipo(TipoConta.CORRENTE)
                .limite(new BigDecimal("500.00"))
                .dataAbertura(LocalDateTime.of(2026, 9, 21, 10, 0))
                .build();

        when(contaService.criarConta(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/contas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{"
                                + "\"correntistaId\":10,"
                                + "\"tipo\":\"CORRENTE\","
                                + "\"limite\":500.00"
                                + "}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/v1/contas/1"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.numero").value("00000001"))
                .andExpect(jsonPath("$.tipo").value("CORRENTE"));
    }

    @Test
    @DisplayName("Deve retornar 404 quando correntistaId nao existir")
    void deveRetornar404QuandoCorrentistaNaoExistir() throws Exception {
        when(contaService.criarConta(any()))
                .thenThrow(RecursoNaoEncontradoException.correntista(99L));

        mockMvc.perform(post("/api/v1/contas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{"
                                + "\"correntistaId\":99,"
                                + "\"tipo\":\"CORRENTE\""
                                + "}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("Deve retornar 400 quando correntistaId ou tipo nao forem informados")
    void deveRetornar400QuandoCamposObrigatoriosAusentes() throws Exception {
        mockMvc.perform(post("/api/v1/contas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("Deve retornar 400 quando poupanca informar limite diferente de zero")
    void deveRetornar400QuandoPoupancaInformaLimite() throws Exception {
        when(contaService.criarConta(any()))
                .thenThrow(RequisicaoInvalidaException.limiteNaoPermitidoParaPoupanca("Limite nao permitido para popuança"));

        mockMvc.perform(post("/api/v1/contas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{"
                                + "\"correntistaId\":10,"
                                + "\"tipo\":\"POUPANCA\","
                                + "\"limite\":100.00"
                                + "}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    // ---------- POST /api/v1/contas/{id}/depositos ----------

    @Test
    @DisplayName("Deve depositar e retornar 201 com transacao e saldoAtual")
    void deveDepositarERetornarSaldoAtual() throws Exception {
        TransacaoResponse response = TransacaoResponse.builder()
                .id(1L)
                .tipoTransacao(TipoTransacao.DEPOSITO)
                .valor(new BigDecimal("100.00"))
                .data(LocalDateTime.now())
                .contaId(5L)
                .saldoAtual(new BigDecimal("100.00"))
                .build();

        when(transacaoService.depositar(eq(5L), any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/contas/5/depositos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"valor\":100.00}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tipoTransacao").value("DEPOSITO"))
                .andExpect(jsonPath("$.saldoAtual").value(100.00));
    }

    @Test
    @DisplayName("Deve retornar 400 quando valor do deposito for zero ou negativo")
    void deveRetornar400QuandoValorDepositoInvalido() throws Exception {
        mockMvc.perform(post("/api/v1/contas/5/depositos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"valor\":0}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Deve retornar 404 quando a conta do deposito nao existir")
    void deveRetornar404QuandoContaDoDepositoNaoExistir() throws Exception {
        when(transacaoService.depositar(eq(999L), any()))
                .thenThrow(RecursoNaoEncontradoException.conta(999L));

        mockMvc.perform(post("/api/v1/contas/999/depositos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"valor\":50.00}"))
                .andExpect(status().isNotFound());
    }

    // ---------- POST /api/v1/contas/{id}/saques ----------

    @Test
    @DisplayName("Deve sacar e retornar 201 com transacao e saldoAtual")
    void deveSacarERetornarSaldoAtual() throws Exception {
        TransacaoResponse response = TransacaoResponse.builder()
                .id(2L)
                .tipoTransacao(TipoTransacao.SAQUE)
                .valor(new BigDecimal("30.00"))
                .data(LocalDateTime.now())
                .contaId(5L)
                .saldoAtual(new BigDecimal("70.00"))
                .build();

        when(transacaoService.sacar(eq(5L), any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/contas/5/saques")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"valor\":30.00}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tipoTransacao").value("SAQUE"))
                .andExpect(jsonPath("$.saldoAtual").value(70.00));
    }

    @Test
    @DisplayName("Deve retornar 422 quando o saque exceder saldo/limite disponivel")
    void deveRetornar422QuandoSaldoInsuficiente() throws Exception {
        when(transacaoService.sacar(eq(5L), any()))
                .thenThrow(SaldoInsuficienteException.paraSaque(
                        new BigDecimal("500.00"), new BigDecimal("100.00")));

        mockMvc.perform(post("/api/v1/contas/5/saques")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"valor\":500.00}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status").value(422));
    }

    @Test
    @DisplayName("Deve retornar 400 quando valor do saque for zero ou negativo")
    void deveRetornar400QuandoValorSaqueInvalido() throws Exception {
        mockMvc.perform(post("/api/v1/contas/5/saques")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"valor\":-10.00}"))
                .andExpect(status().isBadRequest());
    }

    // ---------- POST /api/v1/contas/{id}/rendimento ----------

    @Test
    @DisplayName("Deve aplicar rendimento via body e retornar 201")
    void deveAplicarRendimentoViaBody() throws Exception {
        TransacaoResponse response = TransacaoResponse.builder()
                .id(3L)
                .tipoTransacao(TipoTransacao.RENDIMENTO)
                .valor(new BigDecimal("5.00"))
                .data(LocalDateTime.now())
                .contaId(7L)
                .saldoAtual(new BigDecimal("1005.00"))
                .build();

        when(transacaoService.aplicarRendimento(eq(7L), any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/contas/7/rendimento")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"taxa\":0.005}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tipoTransacao").value("RENDIMENTO"))
                .andExpect(jsonPath("$.saldoAtual").value(1005.00));
    }

    @Test
    @DisplayName("Deve aplicar rendimento via query param quando body nao for enviado")
    void deveAplicarRendimentoViaQueryParam() throws Exception {
        TransacaoResponse response = TransacaoResponse.builder()
                .id(4L)
                .tipoTransacao(TipoTransacao.RENDIMENTO)
                .valor(new BigDecimal("5.00"))
                .data(LocalDateTime.now())
                .contaId(7L)
                .saldoAtual(new BigDecimal("1005.00"))
                .build();

        when(transacaoService.aplicarRendimento(eq(7L), any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/contas/7/rendimento?taxa=0.005"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tipoTransacao").value("RENDIMENTO"));
    }

    @Test
    @DisplayName("Deve retornar 422 quando rendimento for aplicado em conta corrente")
    void deveRetornar422QuandoRendimentoEmContaCorrente() throws Exception {
        when(transacaoService.aplicarRendimento(eq(7L), any()))
                .thenThrow(TipoContaInvalidoException.rendimentoExigePoupanca());

        mockMvc.perform(post("/api/v1/contas/7/rendimento")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"taxa\":0.005}"))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @DisplayName("Deve retornar 422 quando rendimento for pedido antes do periodo minimo de 30 dias")
    void deveRetornar422QuandoRendimentoAntesDoPeriodoMinimo() throws Exception {
        when(transacaoService.aplicarRendimento(eq(7L), any()))
                .thenThrow(PeriodoMinimoNaoAtingidoException.paraOperacao("Rendimento", 12));

        mockMvc.perform(post("/api/v1/contas/7/rendimento")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"taxa\":0.005}"))
                .andExpect(status().isUnprocessableEntity());
    }

    // ---------- POST /api/v1/contas/{id}/juros ----------

    @Test
    @DisplayName("Deve aplicar juros e retornar 201 com saldoAtual")
    void deveAplicarJuros() throws Exception {
        TransacaoResponse response = TransacaoResponse.builder()
                .id(5L)
                .tipoTransacao(TipoTransacao.JUROS)
                .valor(new BigDecimal("6.00"))
                .data(LocalDateTime.now())
                .contaId(9L)
                .saldoAtual(new BigDecimal("-306.00"))
                .build();

        when(transacaoService.aplicarJuros(eq(9L), any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/contas/9/juros")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"taxa\":0.02}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tipoTransacao").value("JUROS"))
                .andExpect(jsonPath("$.saldoAtual").value(-306.00));
    }

    @Test
    @DisplayName("Deve retornar 422 quando juros for aplicado em conta poupanca")
    void deveRetornar422QuandoJurosEmContaPoupanca() throws Exception {
        when(transacaoService.aplicarJuros(eq(9L), any()))
                .thenThrow(TipoContaInvalidoException.jurosExigeCorrente());

        mockMvc.perform(post("/api/v1/contas/9/juros")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"taxa\":0.02}"))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @DisplayName("Deve retornar 404 quando a conta de juros nao existir")
    void deveRetornar404QuandoContaDeJurosNaoExistir() throws Exception {
        when(transacaoService.aplicarJuros(eq(999L), any()))
                .thenThrow(RecursoNaoEncontradoException.conta(999L));

        mockMvc.perform(post("/api/v1/contas/999/juros")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"taxa\":0.02}"))
                .andExpect(status().isNotFound());
    }
}
