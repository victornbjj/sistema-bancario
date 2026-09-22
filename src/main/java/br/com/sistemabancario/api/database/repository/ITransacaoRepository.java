package br.com.sistemabancario.api.database.repository;

import br.com.sistemabancario.api.database.entity.TransacaoEntity;
import br.com.sistemabancario.api.enums.TipoTransacao;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ITransacaoRepository extends JpaRepository<TransacaoEntity, Long> {

    Optional<TransacaoEntity> findTopByContaIdAndTipoOrderByDataDesc(Long contaId, TipoTransacao tipo);

    @Query("SELECT t " +
            "FROM TransacaoEntity t " +
            "WHERE t.conta.id = :contaId " +
            "AND (:tipo IS NULL OR t.tipo = :tipo) " +
            "AND (:dataInicial IS NULL OR t.data >= :dataInicial) " +
            "AND (:dataFinal IS NULL OR t.data <= :dataFinal)")
    Page<TransacaoEntity> buscarExtrato(
            @Param("contaId") Long contaId,
            @Param("tipo") TipoTransacao tipo,
            @Param("dataInicial") LocalDateTime dataInicial,
            @Param("dataFinal") LocalDateTime dataFinal,
            Pageable pageable);

}