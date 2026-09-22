package br.com.sistemabancario.api.database.repository;

import br.com.sistemabancario.api.database.entity.TransacaoEntity;
import br.com.sistemabancario.api.enums.TipoTransacao;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ITransacaoRepository extends JpaRepository<TransacaoEntity, Long> {
   
    Optional<TransacaoEntity> findTopByContaIdAndTipoOrderByDataDesc (Long contaId, TipoTransacao tipo);
}
