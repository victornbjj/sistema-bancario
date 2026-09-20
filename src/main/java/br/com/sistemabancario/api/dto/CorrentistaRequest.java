package br.com.sistemabancario.api.dto;

import lombok.*;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CorrentistaRequest {
    @NotBlank
    private String nome;

    @NotBlank
    private String documento;

    @Email
    private String email;
    private String telefone;
}
