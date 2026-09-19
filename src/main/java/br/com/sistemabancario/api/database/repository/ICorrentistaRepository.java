package br.com.sistemabancario.api.database.repository;


import br.com.sistemabancario.api.database.entity.CorrentistaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ICorrentistaRepository extends JpaRepository<CorrentistaEntity, Long> {
}
