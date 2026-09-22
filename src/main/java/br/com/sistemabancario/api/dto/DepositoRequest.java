package br.com.sistemabancario.api.dto;

import java.math.BigDecimal;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;

import lombok.Data;

@Data
public class DepositoRequest {

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal valor;
}