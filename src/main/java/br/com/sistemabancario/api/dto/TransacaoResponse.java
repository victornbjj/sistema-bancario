package br.com.sistemabancario.api.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;


import br.com.sistemabancario.api.enums.TipoTransacao;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder 
@Getter 
@NoArgsConstructor 
@AllArgsConstructor 
public class TransacaoResponse {
    private long id; 
    private  TipoTransacao tipoTransacao;
    private BigDecimal valor; 
    private  LocalDateTime data; 
    private  Long contaId;
    private BigDecimal saldoAtual;
}
