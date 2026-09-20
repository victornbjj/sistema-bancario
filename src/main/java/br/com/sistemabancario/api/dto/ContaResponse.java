package br.com.sistemabancario.api.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import br.com.sistemabancario.api.enums.TipoConta;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ContaResponse {

    private Long id;
    private String numero;
    private BigDecimal saldo;
    private TipoConta tipo;
    private BigDecimal limite;
    private LocalDateTime dataAbertura;
    private CorrentistaResponse titular;
}
