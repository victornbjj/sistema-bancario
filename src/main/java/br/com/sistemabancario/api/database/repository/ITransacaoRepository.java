package br.com.sistemabancario.api.database.repository;

import br.com.sistemabancario.api.database.entity.TransacaoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ITransacaoRepository extends JpaRepository<TransacaoEntity, Long> {
}
