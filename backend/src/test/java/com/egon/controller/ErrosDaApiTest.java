package com.egon.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/** Erros que não vêm das regras de negócio também saem no formato { codigo, mensagem, campos }. */
@SpringBootTest
@AutoConfigureMockMvc
class ErrosDaApiTest {

    @Autowired MockMvc mvc;

    @Test
    @DisplayName("rota inexistente responde 404 com código")
    void rotaInexistente() throws Exception {
        mvc.perform(get("/nada"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value("RECURSO_NAO_ENCONTRADO"))
                .andExpect(jsonPath("$.campos").isEmpty());
    }

    @Test
    @DisplayName("método não suportado responde 405 com código e os métodos aceitos")
    void metodoNaoSuportado() throws Exception {
        mvc.perform(patch("/alunos/52998224725").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(header().exists("Allow"))
                .andExpect(jsonPath("$.codigo").value("METODO_NAO_PERMITIDO"));
    }

    @Test
    @DisplayName("corpo que não é JSON responde 415 com código")
    void tipoNaoSuportado() throws Exception {
        mvc.perform(post("/empresas").contentType(MediaType.TEXT_PLAIN).content("oi"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.codigo").value("TIPO_NAO_SUPORTADO"));
    }

}
