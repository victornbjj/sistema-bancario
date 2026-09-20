package br.com.sistemabancario.api.dto;

import lombok.*;

import java.time.LocalDateTime;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CorrentistaResponse {
    private Long id;
    private String nome;
    private String documento;
    private String email;
    private String telefone;
    private LocalDateTime dataCadastro;
}
