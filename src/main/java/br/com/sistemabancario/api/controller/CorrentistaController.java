package br.com.sistemabancario.api.controller;

import br.com.sistemabancario.api.dto.CorrentistaRequest;
import br.com.sistemabancario.api.dto.CorrentistaResponse;
import br.com.sistemabancario.api.service.CorrentistaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/correntistas")
public class CorrentistaController {

    @Autowired
    CorrentistaService service;

    @PostMapping("/")
    public ResponseEntity<CorrentistaResponse> criarCorrentista(
            @RequestBody CorrentistaRequest correntistaRequest) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.cadastrar(correntistaRequest));
    }


}
