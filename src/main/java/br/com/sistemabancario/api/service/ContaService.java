package br.com.sistemabancario.api.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import br.com.sistemabancario.api.dto.*;

import br.com.sistemabancario.api.exception.RequisicaoInvalidaException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.sistemabancario.api.database.entity.ContaCorrente;
import br.com.sistemabancario.api.database.entity.ContaEntity;
import br.com.sistemabancario.api.database.entity.ContaPoupanca;
import br.com.sistemabancario.api.database.entity.CorrentistaEntity;
import br.com.sistemabancario.api.database.repository.IContaRepository;
import br.com.sistemabancario.api.database.repository.ICorrentistaRepository;
import br.com.sistemabancario.api.enums.TipoConta;
import br.com.sistemabancario.api.exception.RecursoNaoEncontradoException;
import br.com.sistemabancario.api.exception.TipoContaInvalidoException;
import br.com.sistemabancario.api.exception.ValorInvalidoException;

@Service
public class ContaService {

    private final IContaRepository contaRepository;
    private final ICorrentistaRepository correntistaRepository;

    @Transactional
    public ContaResponse criarConta(ContaRequest request) {
        CorrentistaEntity correntista = correntistaRepository.findById(request.getCorrentistaId())
                .orElseThrow(() -> RecursoNaoEncontradoException.correntista(request.getCorrentistaId()));

        ContaEntity conta;

        if (request.getTipo() == TipoConta.CORRENTE) {
            BigDecimal limite = request.getLimite() != null ? request.getLimite() : BigDecimal.ZERO;

            if (limite.compareTo(BigDecimal.ZERO) < 0) {
                throw new ValorInvalidoException("Limite não pode ser negativo");
            }

            ContaCorrente corrente = new ContaCorrente();
            corrente.setLimite(limite);
            conta = corrente;

        } else if (request.getTipo() == TipoConta.POUPANCA) {
            if (request.getLimite() != null && request.getLimite().compareTo(BigDecimal.ZERO) != 0) {
                throw RequisicaoInvalidaException.limiteNaoPermitidoParaPoupanca("Limite não permitido para conta poupança");
            }
            conta = new ContaPoupanca();

        } else {
            throw new ValorInvalidoException("Tipo de conta inválido");
        }

        conta.setTipo(request.getTipo());
        conta.setCorrentista(correntista);
        conta.setNumero(gerarNumeroConta());

        ContaEntity salva = contaRepository.save(conta);

        return montarContaResponse(salva);
    }

    @Transactional
    public Page<ContaResponse> listar(Long correntistaId, String numero, Pageable pageable) {
        Page<ContaEntity> contas;

        if (correntistaId != null && numero != null) {
            contas = contaRepository.findByCorrentistaIdAndNumero(correntistaId, numero, pageable);
        } else if (correntistaId != null) {
            if (!correntistaRepository.existsById(correntistaId)) {
                throw RecursoNaoEncontradoException.correntista(correntistaId);
            }
            contas = contaRepository.findByCorrentistaId(correntistaId, pageable);
        } else if (numero != null) {
            contas = contaRepository.findByNumero(numero, pageable);
        } else {
            contas = contaRepository.findAll(pageable);
        }

        return contas.map(this::montarContaResponse);
    }

    @Transactional
    public ContaResponse buscarPorId(Long id) {
        ContaEntity conta = contaRepository.findById(id)
                .orElseThrow(() -> RecursoNaoEncontradoException.conta(id));

        return montarContaResponse(conta);
    }

    @Transactional
    public List<ContaResponse> listarPorCorrentista(Long correntistaId) {
        if (!correntistaRepository.existsById(correntistaId)) {
            throw RecursoNaoEncontradoException.correntista(correntistaId);
        }

        return contaRepository.findByCorrentistaId(correntistaId)
                .stream()
                .map(this::montarContaResponse)
                .collect(Collectors.toList());
    }

    private ContaResponse montarContaResponse(ContaEntity conta) {
        CorrentistaEntity correntista = conta.getCorrentista();

        CorrentistaResponse titular = CorrentistaResponse.builder()
                .id(correntista.getId())
                .nome(correntista.getNome())
                .documento(correntista.getDocumento())
                .email(correntista.getEmail())
                .telefone(correntista.getTelefone())
                .dataCadastro(correntista.getDataCadastro())
                .build();

        BigDecimal limite = conta instanceof ContaCorrente ? ((ContaCorrente) conta).getLimite() : null;

        return ContaResponse.builder()
                .id(conta.getId())
                .numero(conta.getNumero())
                .saldo(conta.getSaldo())
                .tipo(conta.getTipo())
                .limite(limite)
                .dataAbertura(conta.getDataAbertura())
                .titular(titular)
                .build();
    }

    private String gerarNumeroConta() {
        String numero;
        do {
            numero = String.format("%08d", ThreadLocalRandom.current().nextLong(100_000_000L));
        } while (contaRepository.existsByNumero(numero));
        return numero;
    }

    public ContaService(IContaRepository contaRepository,
            ICorrentistaRepository correntistaRepository) {
        this.contaRepository = contaRepository;
        this.correntistaRepository = correntistaRepository;
    }

}
