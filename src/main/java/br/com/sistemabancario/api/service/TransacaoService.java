package br.com.sistemabancario.api.service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;

import javax.transaction.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import br.com.sistemabancario.api.database.entity.ContaCorrente;
import br.com.sistemabancario.api.database.entity.ContaEntity;
import br.com.sistemabancario.api.database.entity.ContaPoupanca;
import br.com.sistemabancario.api.database.entity.TransacaoEntity;
import br.com.sistemabancario.api.database.repository.IContaRepository;
import br.com.sistemabancario.api.database.repository.ITransacaoRepository;
import br.com.sistemabancario.api.dto.DepositoRequest;
import br.com.sistemabancario.api.dto.ExtratoItemResponse;
import br.com.sistemabancario.api.dto.SaqueRequest;
import br.com.sistemabancario.api.dto.TransacaoResponse;
import br.com.sistemabancario.api.enums.TipoTransacao;
import br.com.sistemabancario.api.exception.PeriodoMinimoNaoAtingidoException;
import br.com.sistemabancario.api.exception.RecursoNaoEncontradoException;
import br.com.sistemabancario.api.exception.RequisicaoInvalidaException;
import br.com.sistemabancario.api.exception.TipoContaInvalidoException;

@Service
public class TransacaoService {

    private final ITransacaoRepository transacaoRepository;
    private final IContaRepository contaRepository;

    private static final int PERIODO_MINIMO_DIAS = 30;

    @Transactional
    public TransacaoResponse aplicarJuros(Long id, BigDecimal taxa) {
        ContaEntity conta = contaRepository.findById(id)
                .orElseThrow(() -> RecursoNaoEncontradoException.conta(id));

        if (!(conta instanceof ContaCorrente)) {
            throw TipoContaInvalidoException.jurosExigeCorrente();
        }
        ContaCorrente corrente = (ContaCorrente) conta;
        validarPeriodoMinimo(corrente.getId(), TipoTransacao.JUROS, "Juros");
        TransacaoEntity transacao = corrente.aplicarJuros(taxa);

        transacaoRepository.save(transacao);

        return TransacaoResponse.builder()
                .id(transacao.getId())
                .tipoTransacao(transacao.getTipo())
                .valor(transacao.getValor())
                .data(transacao.getData())
                .contaId(corrente.getId())
                .saldoAtual(conta.getSaldo())
                .build();

    }

    @Transactional
    public TransacaoResponse aplicarRendimento(Long Id, BigDecimal taxa) {
        ContaEntity conta = contaRepository.findById(Id)
                .orElseThrow(() -> RecursoNaoEncontradoException.conta(Id));

        if (!(conta instanceof ContaPoupanca)) {
            throw TipoContaInvalidoException.rendimentoExigePoupanca();
        }

        ContaPoupanca poupanca = (ContaPoupanca) conta;
        validarPeriodoMinimo(Id, TipoTransacao.RENDIMENTO, "Rendimento");

        TransacaoEntity transacao = poupanca.aplicarRendimentoMensal(taxa);

        transacaoRepository.save(transacao);

        return TransacaoResponse.builder()
                .id(poupanca.getId())
                .tipoTransacao(transacao.getTipo())
                .valor(transacao.getValor())
                .data(transacao.getData())
                .contaId(conta.getId())
                .saldoAtual(conta.getSaldo())
                .build();

    }

    @Transactional
    public Page<ExtratoItemResponse> consultarExtrato(Long contaId, TipoTransacao tipo,
            LocalDateTime dataInicial, LocalDateTime dataFinal, Pageable pageable) {

        if (!contaRepository.existsById(contaId)) {
            throw RecursoNaoEncontradoException.conta(contaId);
        }

        if (dataInicial != null && dataFinal != null && dataInicial.isAfter(dataFinal)) {
            throw RequisicaoInvalidaException.periodoInvalido();
        }

        Page<TransacaoEntity> transacoes = transacaoRepository.buscarExtrato(
                contaId, tipo, dataInicial, dataFinal, pageable);

        return transacoes.map(t -> ExtratoItemResponse.builder()
                .id(t.getId())
                .tipo(t.getTipo())
                .valor(t.getValor())
                .data(t.getData())
                .build());
    }

    @Transactional
    public TransacaoResponse sacar(Long idConta, SaqueRequest request) {
        ContaEntity conta = contaRepository.findById(idConta)
                .orElseThrow(() -> RecursoNaoEncontradoException.conta(idConta));

        TransacaoEntity transacao = conta.sacar(request.getValor());

        transacaoRepository.save(transacao);

        return TransacaoResponse.builder()
                .id(transacao.getId())
                .tipoTransacao(TipoTransacao.SAQUE)
                .valor(transacao.getValor())
                .data(transacao.getData())
                .contaId(conta.getId())
                .saldoAtual(conta.getSaldo())
                .build();
    }

    @Transactional
    public TransacaoResponse depositar(Long idCOnta, DepositoRequest request) {
        ContaEntity conta = contaRepository.findById(idCOnta)
                .orElseThrow(() -> RecursoNaoEncontradoException.conta(idCOnta));

        TransacaoEntity transacao = conta.depositar(request.getValor());
        transacaoRepository.save(transacao);

        return TransacaoResponse.builder()
                .id(transacao.getId())
                .tipoTransacao(TipoTransacao.DEPOSITO)
                .valor(transacao.getValor())
                .data(transacao.getData())
                .contaId(conta.getId())
                .saldoAtual(conta.getSaldo())
                .build();
    }

    private void validarPeriodoMinimo(Long contaId, TipoTransacao tipo, String operacao) {
        transacaoRepository.findTopByContaIdAndTipoOrderByDataDesc(contaId, tipo)
                .ifPresent(ultima -> {
                    long diasDesdeUltima = Duration.between(ultima.getData(), LocalDateTime.now()).toDays();
                    if (diasDesdeUltima < PERIODO_MINIMO_DIAS) {
                        throw PeriodoMinimoNaoAtingidoException.paraOperacao(
                                operacao, PERIODO_MINIMO_DIAS - diasDesdeUltima);
                    }
                });
    }

    public TransacaoService(ITransacaoRepository transacaoRepository, IContaRepository contaRepository) {
        this.transacaoRepository = transacaoRepository;
        this.contaRepository = contaRepository;

    }
}
