package br.com.sistemabancario.api.database.repository;


import br.com.sistemabancario.api.database.entity.CorrentistaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface ICorrentistaRepository extends JpaRepository<CorrentistaEntity, Long> {

    @Override
    Optional<CorrentistaEntity> findById(Long id);

    Boolean existsByEmail(String email);

    Boolean existsByDocumento(String documento);

    Boolean existsByTelefone(String telefone);

    Page<CorrentistaEntity> findByDocumentoContaining(String documento, Pageable pageable);

}
