package br.com.sistemabancario.api.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter 
@Builder 
@NoArgsConstructor 
@AllArgsConstructor 
public class RendimentoRequest {
     private BigDecimal taxa;
    
}
