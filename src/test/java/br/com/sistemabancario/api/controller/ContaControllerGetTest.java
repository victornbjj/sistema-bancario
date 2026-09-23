package br.com.sistemabancario.api.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;


import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.web.servlet.MockMvc;

import br.com.sistemabancario.api.dto.ContaResponse;
import br.com.sistemabancario.api.dto.ExtratoItemResponse;
import br.com.sistemabancario.api.enums.TipoConta;
import br.com.sistemabancario.api.enums.TipoTransacao;
import br.com.sistemabancario.api.exception.RecursoNaoEncontradoException;
import br.com.sistemabancario.api.service.ContaService;
import br.com.sistemabancario.api.service.TransacaoService;

/**
 * Testes de contrato para os endpoints GET do ContaController:
 * GET /contas/{id}, GET /contas (com filtros correntistaId/numero) e
 * GET /contas/{contaId}/extrato (com filtros tipo/dataInicial/dataFinal).
 */
@WebMvcTest(ContaController.class)
class ContaControllerGetTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ContaService contaService;

    @MockBean
    private TransacaoService transacaoService;

    // ---------- GET /api/v1/contas/{id} ----------

    @Test
    @DisplayName("Deve buscar conta por id e retornar 200 com saldo, tipo e titular")
    void deveBuscarContaPorId() throws Exception {
        ContaResponse response = contaBase(1L, TipoConta.CORRENTE, "100.00");

        when(contaService.buscarPorId(1L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/contas/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.saldo").value(100.00))
                .andExpect(jsonPath("$.tipo").value("CORRENTE"))
                .andExpect(jsonPath("$.titular.id").value(10));
    }

    @Test
    @DisplayName("Deve retornar 404 quando a conta buscada por id nao existir")
    void deveRetornar404QuandoContaNaoExistir() throws Exception {
        when(contaService.buscarPorId(999L))
                .thenThrow(RecursoNaoEncontradoException.conta(999L));

        mockMvc.perform(get("/api/v1/contas/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    // ---------- GET /api/v1/contas (filtros e paginacao) ----------

    @Test
    @DisplayName("Deve listar contas sem filtro e retornar 200 paginado")
    void deveListarContasSemFiltro() throws Exception {
        Page<ContaResponse> pagina = new PageImpl<>(
                Arrays.asList(contaBase(1L, TipoConta.CORRENTE, "100.00"),
                        contaBase(2L, TipoConta.POUPANCA, "50.00")));

        when(contaService.listar(isNull(), isNull(), any())).thenReturn(pagina);

        mockMvc.perform(get("/api/v1/contas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2));
    }

    @Test
    @DisplayName("Deve listar contas filtrando por correntistaId")
    void deveListarContasPorCorrentistaId() throws Exception {
        Page<ContaResponse> pagina = new PageImpl<>(
                Arrays.asList(contaBase(1L, TipoConta.CORRENTE, "100.00")));

        when(contaService.listar(eq(10L), isNull(), any())).thenReturn(pagina);

        mockMvc.perform(get("/api/v1/contas").param("correntistaId", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].titular.id").value(10));
    }

    @Test
    @DisplayName("Deve listar contas filtrando por numero")
    void deveListarContasPorNumero() throws Exception {
        Page<ContaResponse> pagina = new PageImpl<>(
                Arrays.asList(contaBase(1L, TipoConta.CORRENTE, "100.00")));

        when(contaService.listar(isNull(), eq("00000001"), any())).thenReturn(pagina);

        mockMvc.perform(get("/api/v1/contas").param("numero", "00000001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].numero").value("00000001"));
    }

    @Test
    @DisplayName("Deve retornar 404 quando correntistaId do filtro nao existir")
    void deveRetornar404QuandoCorrentistaDoFiltroNaoExistir() throws Exception {
        when(contaService.listar(eq(999L), isNull(), any()))
                .thenThrow(RecursoNaoEncontradoException.correntista(999L));

        mockMvc.perform(get("/api/v1/contas").param("correntistaId", "999"))
                .andExpect(status().isNotFound());
    }

    // ---------- GET /api/v1/contas/{contaId}/extrato ----------

    @Test
    @DisplayName("Deve consultar extrato sem filtros e retornar 200 paginado")
    void deveConsultarExtratoSemFiltros() throws Exception {
        Page<ExtratoItemResponse> pagina = new PageImpl<>(Arrays.asList(
                ExtratoItemResponse.builder()
                        .id(1L).tipo(TipoTransacao.DEPOSITO)
                        .valor(new BigDecimal("100.00"))
                        .data(LocalDateTime.of(2026, 9, 20, 10, 0))
                        .build(),
                ExtratoItemResponse.builder()
                        .id(2L).tipo(TipoTransacao.SAQUE)
                        .valor(new BigDecimal("30.00"))
                        .data(LocalDateTime.of(2026, 9, 21, 9, 0))
                        .build()));

        when(transacaoService.consultarExtrato(eq(1L), isNull(), isNull(), isNull(), any()))
                .thenReturn(pagina);

        mockMvc.perform(get("/api/v1/contas/1/extrato"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2));
    }

    @Test
    @DisplayName("Deve consultar extrato filtrando por tipo e periodo")
    void deveConsultarExtratoComFiltros() throws Exception {
        Page<ExtratoItemResponse> pagina = new PageImpl<>(Arrays.asList(
                ExtratoItemResponse.builder()
                        .id(2L).tipo(TipoTransacao.SAQUE)
                        .valor(new BigDecimal("30.00"))
                        .data(LocalDateTime.of(2026, 9, 21, 9, 0))
                        .build()));

        when(transacaoService.consultarExtrato(
                eq(1L),
                eq(TipoTransacao.SAQUE),
                eq(LocalDateTime.of(2026, 9, 1, 0, 0)),
                eq(LocalDateTime.of(2026, 9, 22, 0, 0)),
                any()))
                .thenReturn(pagina);

        mockMvc.perform(get("/api/v1/contas/1/extrato")
                        .param("tipo", "SAQUE")
                        .param("dataInicial", "2026-09-01T00:00:00")
                        .param("dataFinal", "2026-09-22T00:00:00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].tipo").value("SAQUE"));
    }

    @Test
    @DisplayName("Deve retornar 404 quando a conta do extrato nao existir")
    void deveRetornar404QuandoContaDoExtratoNaoExistir() throws Exception {
        when(transacaoService.consultarExtrato(eq(999L), isNull(), isNull(), isNull(), any()))
                .thenThrow(RecursoNaoEncontradoException.conta(999L));

        mockMvc.perform(get("/api/v1/contas/999/extrato"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Deve retornar 400 quando dataInicial for posterior a dataFinal")
    void deveRetornar400QuandoPeriodoInvalido() throws Exception {
        when(transacaoService.consultarExtrato(eq(1L), isNull(), any(), any(), any()))
                .thenThrow(br.com.sistemabancario.api.exception.RequisicaoInvalidaException.periodoInvalido());

        mockMvc.perform(get("/api/v1/contas/1/extrato")
                        .param("dataInicial", "2026-09-22T00:00:00")
                        .param("dataFinal", "2026-09-01T00:00:00"))
                .andExpect(status().isBadRequest());
    }

    // ---------- helper ----------

    private ContaResponse contaBase(Long id, TipoConta tipo, String saldo) {
        return ContaResponse.builder()
                .id(id)
                .numero(String.format("%08d", id))
                .saldo(new BigDecimal(saldo))
                .tipo(tipo)
                .limite(tipo == TipoConta.CORRENTE ? new BigDecimal("500.00") : null)
                .dataAbertura(LocalDateTime.of(2026, 9, 1, 8, 0))
                .titular(br.com.sistemabancario.api.dto.CorrentistaResponse.builder()
                        .id(10L)
                        .nome("Maria Silva")
                        .documento("12345678900")
                        .build())
                .build();
    }
}
