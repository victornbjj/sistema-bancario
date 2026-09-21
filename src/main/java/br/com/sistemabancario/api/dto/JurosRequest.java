package br.com.sistemabancario.api.dto;

import java.math.BigDecimal;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Getter
@Builder 
@NoArgsConstructor
@AllArgsConstructor
public class JurosRequest {
    private BigDecimal taxa;
}