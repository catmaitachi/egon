package com.egon.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

/** As listas suspensas do cadastro de aluno, carregadas na subida da aplicação. */
@SpringBootTest
@AutoConfigureMockMvc
class ListasDoCadastroTest {

    @Autowired MockMvc mvc;

    @Test
    @DisplayName("lista as instituições conveniadas em ordem alfabética")
    void instituicoes() throws Exception {
        mvc.perform(get("/instituicoes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].nome").value("Centro Universitário Vale Verde"))
                .andExpect(jsonPath("$[0].cnpj").value("45218073000192"));
    }

    @Test
    @DisplayName("lista os cursos em ordem alfabética")
    void cursos() throws Exception {
        mvc.perform(get("/cursos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(8)))
                .andExpect(jsonPath("$[0].nome").value("Administração"));
    }

}
