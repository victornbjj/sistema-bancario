package br.com.sistemabancario.api.controller;

import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDateTime;

import javax.validation.Valid;

import br.com.sistemabancario.api.dto.*;
import br.com.sistemabancario.api.enums.TipoTransacao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import br.com.sistemabancario.api.service.ContaService;
import br.com.sistemabancario.api.service.TransacaoService;

@RestController
@RequestMapping("/api/v1/contas")
public class ContaController {

    private final ContaService contaService;
    private final TransacaoService transacaoService;

    @PostMapping
    public ResponseEntity<ContaResponse> criarConta(@Valid @RequestBody ContaRequest request) {
        ContaResponse response = contaService.criarConta(request);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.getId())
                .toUri();

        return ResponseEntity.created(location).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ContaResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(contaService.buscarPorId(id));
    }

    

    @GetMapping
    public ResponseEntity<Page<ContaResponse>> listar(
            @RequestParam(required = false) Long correntistaId,
            @RequestParam(required = false) String numero,
            @PageableDefault(size = 10, sort = "dataAbertura") Pageable pageable) {
        return ResponseEntity.ok(contaService.listar(correntistaId, numero, pageable));
    }

    @PostMapping("/{id}/depositos")
    public ResponseEntity<TransacaoResponse> depositar(@PathVariable Long id,
            @Valid @RequestBody DepositoRequest request) {
        TransacaoResponse resposta = transacaoService.depositar(id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(resposta);
    }

     @GetMapping("/{contaId}/extrato")
    public ResponseEntity<Page<ExtratoItemResponse>> consultarExtrato(
            @PathVariable Long contaId,
            @RequestParam(required = false) TipoTransacao tipo,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime dataInicial,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime dataFinal,
            Pageable pageable) {

        Page<ExtratoItemResponse> extrato = transacaoService.consultarExtrato(
                contaId,
                tipo,
                dataInicial,
                dataFinal,
                pageable
        );

        return ResponseEntity.ok(extrato);
    }


    @PostMapping("/{id}/saques")
    public ResponseEntity<TransacaoResponse> sacar(@PathVariable Long id, @Valid @RequestBody SaqueRequest request) {
        TransacaoResponse resposta = transacaoService.sacar(id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(resposta);
    }

    @PostMapping("/{id}/rendimento")
    public ResponseEntity<TransacaoResponse> aplicarRendimento(
            @PathVariable Long id,
            @RequestParam(required = false) BigDecimal taxa,
            @RequestBody(required = false) RendimentoRequest body) {

        BigDecimal taxaFinal;

        if (taxa != null) {
            taxaFinal = taxa;
        } else {
            if (body != null) {
                taxaFinal = body.getTaxa();
            } else {
                taxaFinal = null;
            }

        }

        TransacaoResponse resposta = transacaoService.aplicarRendimento(id, taxaFinal);
        return ResponseEntity.status(HttpStatus.CREATED).body(resposta);
    }

    @PostMapping("/{id}/juros")
    public ResponseEntity<TransacaoResponse> aplicarJuros(
            @PathVariable Long id,
            @RequestParam(required = false) BigDecimal taxa,
            @RequestBody(required = false) JurosRequest body) {

        BigDecimal taxaFinal;

        if (taxa != null) {
            taxaFinal = taxa;
        }

        else if (body != null) {
            taxaFinal = body.getTaxa();
        }

        else {
            taxaFinal = null;
        }

        TransacaoResponse resposta = transacaoService.aplicarJuros(id, taxaFinal);
        return ResponseEntity.status(HttpStatus.CREATED).body(resposta);
    }

    public ContaController(ContaService contaService, TransacaoService transacaoService) {
        this.contaService = contaService;
        this.transacaoService = transacaoService;
    }

}
