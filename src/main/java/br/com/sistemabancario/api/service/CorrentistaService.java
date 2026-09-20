package br.com.sistemabancario.api.service;

import br.com.sistemabancario.api.database.entity.CorrentistaEntity;
import br.com.sistemabancario.api.database.repository.ICorrentistaRepository;
import br.com.sistemabancario.api.dto.CorrentistaRequest;
import br.com.sistemabancario.api.dto.CorrentistaResponse;
import br.com.sistemabancario.api.exception.BusinessException;
import br.com.sistemabancario.api.exception.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Service
public class CorrentistaService {

    ICorrentistaRepository repository;

    public CorrentistaService(ICorrentistaRepository repository) {
        this.repository = repository;
    }


    @Transactional
    public CorrentistaResponse cadastrar(CorrentistaRequest request) {
        String documento = request.getDocumento().trim();
        String email = normalizarOpcional(request.getEmail());
        String telefone = normalizarOpcional(request.getTelefone());

        if (repository.existsByDocumento(documento)) {
            throw new BusinessException("Documento já cadastrado");
        }

        if (email != null && repository.existsByEmail(email)) {
            throw new BusinessException("Email já cadastrado");
        }

        if (telefone != null && repository.existsByTelefone(telefone)) {
            throw new BusinessException("Telefone já cadastrado");
        }

        CorrentistaEntity entity = new CorrentistaEntity();
        entity.setNome(request.getNome().trim());
        entity.setDocumento(documento);
        entity.setEmail(email);
        entity.setTelefone(telefone);

        CorrentistaEntity correntista = repository.save(entity);

        return converterParaReponse(correntista);


    }

    @Transactional
    public CorrentistaResponse buscarPorId(Long id) {
        CorrentistaEntity entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Corretista não encontrado"));


        return converterParaReponse(entity);

    }


    public Page<CorrentistaResponse> listar(String documento, Pageable pageable) {
        String filtro = documento == null ? "" : documento.trim();

        Page<CorrentistaEntity> correntistas = filtro.isEmpty()
                ? repository.findAll(pageable)
                : repository.findByDocumentoContaining(filtro, pageable);

        return correntistas.map(this::converterParaReponse);
    }



    private String normalizarOpcional(String valor) {
        if (valor == null) {
            return null;
        }
        String valorNormalizado = valor.trim();
        return valorNormalizado.isEmpty() ? null : valorNormalizado;

    }

    private CorrentistaResponse converterParaReponse(CorrentistaEntity correntistaEntity){
        return CorrentistaResponse.builder()
                .id(correntistaEntity.getId())
                .nome(correntistaEntity.getNome())
                .documento(correntistaEntity.getDocumento())
                .email(correntistaEntity.getEmail())
                .telefone(correntistaEntity.getTelefone())
                .dataCadastro(correntistaEntity.getDataCadastro())
                .build();
    }
}