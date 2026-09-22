package br.com.sistemabancario.api.service;

import br.com.sistemabancario.api.database.entity.CorrentistaEntity;
import br.com.sistemabancario.api.database.repository.ICorrentistaRepository;
import br.com.sistemabancario.api.dto.CorrentistaRequest;
import br.com.sistemabancario.api.dto.CorrentistaResponse;
import br.com.sistemabancario.api.exception.RecursoNaoEncontradoException;
import br.com.sistemabancario.api.exception.RegistroDuplicadoException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Service
public class CorrentistaService {

    private final ICorrentistaRepository repository;

   

    @Transactional
    public CorrentistaResponse cadastrar(CorrentistaRequest request) {
        String documento = request.getDocumento().trim();
        String email = normalizarOpcional(request.getEmail());
        String telefone = normalizarOpcional(request.getTelefone());

        if (repository.existsByDocumento(documento)) {
            throw RegistroDuplicadoException.documento(documento);
        }

        if (email != null && repository.existsByEmail(email)) {
            throw RegistroDuplicadoException.email(email);
        }

        if (telefone != null && repository.existsByTelefone(telefone)) {
            throw RegistroDuplicadoException.telefone(telefone);
        }

        CorrentistaEntity entity = new CorrentistaEntity();
        entity.setNome(request.getNome().trim());
        entity.setDocumento(documento);
        entity.setEmail(email);
        entity.setTelefone(telefone);

        CorrentistaEntity correntista = repository.save(entity);

        return converterParaReponse(correntista);


    }

    @Transactional()
    public CorrentistaResponse buscarPorId(Long id) {
        CorrentistaEntity entity = repository.findById(id)
                .orElseThrow(() -> RecursoNaoEncontradoException.correntista(id));


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

     public CorrentistaService(ICorrentistaRepository repository) {
        this.repository = repository;
    }

}