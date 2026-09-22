package br.com.sistemabancario.api.controller;

import br.com.sistemabancario.api.dto.ContaResponse;
import br.com.sistemabancario.api.dto.CorrentistaRequest;
import br.com.sistemabancario.api.dto.CorrentistaResponse;
import br.com.sistemabancario.api.service.ContaService;
import br.com.sistemabancario.api.service.CorrentistaService;

import java.net.URI;
import java.util.List;

import javax.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/api/v1/correntistas")
public class CorrentistaController {

    private final CorrentistaService service;
    private final ContaService contaService;

    @PostMapping
    public ResponseEntity<CorrentistaResponse> criarCorrentista(
            @Valid @RequestBody CorrentistaRequest correntistaRequest) {

        CorrentistaResponse response = service.cadastrar(correntistaRequest);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.getId())
                .toUri();

        return ResponseEntity
                .created(location)
                .body(response);

    }

    @GetMapping
    public ResponseEntity<Page<CorrentistaResponse>> listar(@RequestParam(required = false) String documento,
            @PageableDefault(size = 10, sort = "nome") Pageable pageable) {
        Page<CorrentistaResponse> resultado = service.listar(documento, pageable);
        return ResponseEntity.ok(resultado);
    }

    @GetMapping("{id}")
    public ResponseEntity<CorrentistaResponse> buscarPorID(@PathVariable Long id) {
        CorrentistaResponse response = service.buscarPorId(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/contas")
    public ResponseEntity<List<ContaResponse>> listarContas(@PathVariable Long id) {
        return ResponseEntity.ok(contaService.listarPorCorrentista(id));
    }

    public CorrentistaController(CorrentistaService service, ContaService contaService) {
        this.service = service;
        this.contaService = contaService;
    }

}
