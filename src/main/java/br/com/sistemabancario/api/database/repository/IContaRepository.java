package br.com.sistemabancario.api.database.repository;

import br.com.sistemabancario.api.database.entity.ContaEntity;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IContaRepository extends JpaRepository<ContaEntity, Long> {

    boolean existsByNumero(String numero);
    
    Page<ContaEntity> findByCorrentistaIdAndNumero(Long correntistaId, String numero, Pageable pageable);
    Page<ContaEntity> findByCorrentistaId(Long correntistaId, Pageable pageable);
    Page<ContaEntity> findByNumero(String numero, Pageable pageable);
    List<ContaEntity> findByCorrentistaId(Long correntistaId);

}
