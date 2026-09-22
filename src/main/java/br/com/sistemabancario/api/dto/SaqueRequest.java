package br.com.sistemabancario.api.dto;

import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class SaqueRequest {

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal valor;
}
