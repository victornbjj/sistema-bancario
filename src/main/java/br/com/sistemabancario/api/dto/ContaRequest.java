package br.com.sistemabancario.api.dto;

import java.math.BigDecimal;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;

import br.com.sistemabancario.api.enums.TipoConta;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ContaRequest {

    @NotNull
    private Long correntistaId;

    @NotNull
    private TipoConta tipo;

    @DecimalMin(value = "0.00", inclusive = true)
    private BigDecimal limite;
}
