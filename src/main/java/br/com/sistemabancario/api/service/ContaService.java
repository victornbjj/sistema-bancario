package br.com.sistemabancario.api.service;

import java.math.BigDecimal;
import java.util.concurrent.ThreadLocalRandom;

import javax.transaction.Transactional;

import org.springframework.stereotype.Service;

import br.com.sistemabancario.api.database.entity.ContaCorrente;
import br.com.sistemabancario.api.database.entity.ContaEntity;
import br.com.sistemabancario.api.database.entity.ContaPoupanca;
import br.com.sistemabancario.api.database.entity.CorrentistaEntity;
import br.com.sistemabancario.api.database.entity.TransacaoEntity;
import br.com.sistemabancario.api.database.repository.IContaRepository;
import br.com.sistemabancario.api.database.repository.ICorrentistaRepository;
import br.com.sistemabancario.api.database.repository.ITransacaoRepository;
import br.com.sistemabancario.api.dto.ContaRequest;
import br.com.sistemabancario.api.dto.ContaResponse;
import br.com.sistemabancario.api.dto.CorrentistaResponse;
import br.com.sistemabancario.api.dto.TransacaoResponse;
import br.com.sistemabancario.api.enums.TipoConta;
import br.com.sistemabancario.api.exception.RecursoNaoEncontradoException;
import br.com.sistemabancario.api.exception.TipoContaInvalidoException;
import br.com.sistemabancario.api.exception.ValorInvalidoException;

@Service 
public class ContaService {
    

    private  final ITransacaoRepository transacaoRepository; 
    private  final IContaRepository contaRepository;
    private final ICorrentistaRepository correntistaRepository;
    
    
    public ContaService(ITransacaoRepository transacaoRepository, IContaRepository contaRepository, ICorrentistaRepository correntistaRepository){
        this.transacaoRepository= transacaoRepository;
        this.contaRepository = contaRepository;
        this.correntistaRepository = correntistaRepository;
    }
    
    @Transactional 
    public ContaResponse criarConta (ContaRequest request){
             CorrentistaEntity correntista = correntistaRepository.findById(request.getCorrentistaId())
            .orElseThrow(() -> RecursoNaoEncontradoException.correntista(request.getCorrentistaId()));

    ContaEntity conta;
    BigDecimal limiteResposta = null;

    if (request.getTipo() == TipoConta.CORRENTE) {
        BigDecimal limite = request.getLimite() != null ? request.getLimite() : BigDecimal.ZERO;

        if (limite.compareTo(BigDecimal.ZERO) < 0) {
            throw new ValorInvalidoException("Limite não pode ser negativo");
        }

        ContaCorrente corrente = new ContaCorrente();
        corrente.setLimite(limite);
        conta = corrente;
        limiteResposta = limite;

    } else if (request.getTipo() == TipoConta.POUPANCA) {
        if (request.getLimite() != null && request.getLimite().compareTo(BigDecimal.ZERO) != 0) {
            throw new TipoContaInvalidoException("Conta poupança não possui limite.");
        }
        conta = new ContaPoupanca();

    } else {
        throw new ValorInvalidoException("Tipo de conta inválido");
    }

    conta.setTipo(request.getTipo());
    conta.setCorrentista(correntista);
    conta.setNumero(gerarNumeroConta());

    ContaEntity salva = contaRepository.save(conta);

    CorrentistaResponse titular = CorrentistaResponse.builder()
            .id(correntista.getId())
            .nome(correntista.getNome())
            .documento(correntista.getDocumento())
            .email(correntista.getEmail())
            .telefone(correntista.getTelefone())
            .dataCadastro(correntista.getDataCadastro())
            .build();

    return ContaResponse.builder()
            .id(salva.getId())
            .numero(salva.getNumero())
            .saldo(salva.getSaldo())
            .tipo(salva.getTipo())
            .limite(limiteResposta)
            .dataAbertura(salva.getDataAbertura())
            .titular(titular)
            .build();

     }
     
    @Transactional
    public TransacaoResponse aplicarRendimento(Long Id, BigDecimal taxa){
        ContaEntity conta = contaRepository.findById(Id)
        .orElseThrow(() -> RecursoNaoEncontradoException.conta(Id));

        if (!(conta instanceof ContaPoupanca)) {
            throw  TipoContaInvalidoException.rendimentoExigePoupanca();
        }

        ContaPoupanca poupanca = (ContaPoupanca) conta;

        TransacaoEntity transacao = poupanca.aplicarRendimentoMensal(taxa);
        
        transacaoRepository.save(transacao);

        return TransacaoResponse.builder()
        .id(poupanca.getId())
        .tipoTransacao(transacao.getTipo())
        .valor(transacao.getValor())
        .data(transacao.getData())
        .contaId(conta.getId())
        .build();
       
    }
    
    @Transactional
    public TransacaoResponse aplicarJuros(Long id, BigDecimal taxa) {
    ContaEntity conta = contaRepository.findById(id)
            .orElseThrow(() -> RecursoNaoEncontradoException.conta(id));

    if (!(conta instanceof ContaCorrente)) {
        throw TipoContaInvalidoException.jurosExigeCorrente();
    }
    ContaCorrente corrente = (ContaCorrente) conta;

    TransacaoEntity transacao = corrente.aplicarJuros(taxa);
    transacaoRepository.save(transacao);

    return TransacaoResponse.builder()
            .id(transacao.getId())
            .tipoTransacao(transacao.getTipo())
            .valor(transacao.getValor())
            .data(transacao.getData())
            .contaId(corrente.getId())
            .build();

    }


    private String gerarNumeroConta() {
    String numero;
    do {
        numero = String.format("%08d", ThreadLocalRandom.current().nextLong(100_000_000L));
    } while (contaRepository.existsByNumero(numero));
    return numero;
}
       
}  
