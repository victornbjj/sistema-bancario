package br.com.sistemabancario.api.controller;

import java.math.BigDecimal;
import java.net.URI;

import javax.validation.Valid;

import br.com.sistemabancario.api.dto.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import br.com.sistemabancario.api.service.ContaService;


@RestController
@RequestMapping("/api/v1/contas")
public class ContaController {
   
  
    private final ContaService service;
     
    public ContaController(ContaService service){
        this.service = service;
    }




    @PostMapping
    public ResponseEntity<ContaResponse> criarConta(@Valid @RequestBody ContaRequest request) {
    ContaResponse response = service.criarConta(request);

    URI location = ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(response.getId())
            .toUri();

    return ResponseEntity.created(location).body(response);
    }

    @PostMapping("/{id}/depositos")
    public ResponseEntity<TransacaoResponse> depositar(@PathVariable Long id, @Valid @RequestBody DepositoRequest request) {
        TransacaoResponse resposta = service.depositar(id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(resposta);
    }

    @PostMapping("/{id}/saques")
    public ResponseEntity<TransacaoResponse> sacar(@PathVariable Long id, @Valid @RequestBody SaqueRequest request) {
        TransacaoResponse resposta = service.sacar(id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(resposta);
    }


   @PostMapping("/{id}/rendimento")
   public ResponseEntity<TransacaoResponse> aplicarRendimento(
        @PathVariable Long id,
        @RequestParam(required = false) BigDecimal taxa,
        @RequestBody (required = false) RendimentoRequest body) {
     
    BigDecimal taxaFinal;

     if (taxa != null) {
             taxaFinal = taxa;} 
      else {
        if (body != null){
         taxaFinal = body.getTaxa();}
        else {
          taxaFinal = null;}

      }

    TransacaoResponse resposta = service.aplicarRendimento(id, taxaFinal);
    return ResponseEntity.status(HttpStatus.CREATED).body(resposta);
   }

   @PostMapping ("/{id}/juros")
   public ResponseEntity<TransacaoResponse> aplicarJuros(
        @PathVariable Long id,
        @RequestParam(required = false) BigDecimal taxa,
        @RequestBody (required = false) JurosRequest body) {
     
    
    BigDecimal taxaFinal;

    if (taxa != null) {
    taxaFinal = taxa;}
    
    else if (body != null) {
    taxaFinal = body.getTaxa();} 
    
    else {
    taxaFinal = null;}

    TransacaoResponse resposta = service.aplicarJuros(id, taxaFinal);
    return ResponseEntity.status(HttpStatus.CREATED).body(resposta);
   }


}
