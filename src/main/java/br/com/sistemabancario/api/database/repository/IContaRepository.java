package br.com.sistemabancario.api.database.repository;

import br.com.sistemabancario.api.database.entity.ContaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IContaRepository extends JpaRepository<ContaEntity, Long> {
}
