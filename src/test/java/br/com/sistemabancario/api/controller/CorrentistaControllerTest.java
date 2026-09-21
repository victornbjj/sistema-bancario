package br.com.sistemabancario.api.controller;

import br.com.sistemabancario.api.dto.CorrentistaResponse;
import br.com.sistemabancario.api.exception.RegistroDuplicadoException;
import br.com.sistemabancario.api.service.CorrentistaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CorrentistaController.class)
class CorrentistaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CorrentistaService service;

    @Test
    void deveCadastrarCorrentistaERetornarLocation() throws Exception {
        CorrentistaResponse response = CorrentistaResponse.builder()
                .id(1L)
                .nome("Maria Silva")
                .documento("12345678900")
                .email("maria@email.com")
                .telefone("11999999999")
                .dataCadastro(LocalDateTime.of(2026, 9, 21, 10, 0))
                .build();

        when(service.cadastrar(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/correntistas/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{"
                                + "\"nome\":\"Maria Silva\","
                                + "\"documento\":\"12345678900\","
                                + "\"email\":\"maria@email.com\","
                                + "\"telefone\":\"11999999999\""
                                + "}"))
                .andExpect(status().isCreated())
                .andExpect(header().string(
                        "Location",
                        "http://localhost/api/v1/correntistas/1"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("Maria Silva"))
                .andExpect(jsonPath("$.documento").value("12345678900"));
    }

    @Test
    void deveRetornar400QuandoDocumentoNaoForInformado() throws Exception {
        mockMvc.perform(post("/api/v1/correntistas/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{"
                                + "\"nome\":\"Maria Silva\","
                                + "\"email\":\"maria@email.com\""
                                + "}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.fieldErrors[0].field").value("documento"));
    }

    @Test
    void deveRetornar409QuandoDocumentoJaExistir() throws Exception {
        when(service.cadastrar(any()))
                .thenThrow(RegistroDuplicadoException.documento("12345678900"));

        mockMvc.perform(post("/api/v1/correntistas/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{"
                                + "\"nome\":\"Maria Silva\","
                                + "\"documento\":\"12345678900\""
                                + "}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Registro Duplicado"));
    }
}