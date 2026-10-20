package com.egon.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;

/** HU08: a empresa cadastra vantagens com foto; o catálogo as mostra (HU09). */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class VantagemControllerTest extends ApiTestSupport {

    private String livraria;

    @BeforeEach
    void cenario() throws Exception {
        cadastrarEmpresa(CNPJ_LIVRARIA, "livraria");
        livraria = entrar("livraria", "segredo123");
    }

    @Test
    @DisplayName("cadastra com foto, lista no catálogo com a empresa e serve a foto")
    void cadastraEListaNoCatalogo() throws Exception {
        int id = cadastrarVantagem(livraria, "Almoço com desconto", 120);

        mvc.perform(get("/vantagens"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].nome").value("Almoço com desconto"))
                .andExpect(jsonPath("$[0].custo").value(120))
                .andExpect(jsonPath("$[0].tipo").value("DESCONTO"))
                .andExpect(jsonPath("$[0].empresa.nome").value("Empresa livraria"))
                .andExpect(jsonPath("$[0].fotoUrl").value("/vantagens/" + id + "/foto"));

        mvc.perform(get("/vantagens/" + id + "/foto"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_PNG))
                .andExpect(content().bytes(PNG));
    }

    @Test
    @DisplayName("exige custo inteiro maior que zero e todos os campos")
    void validaCampos() throws Exception {
        mvc.perform(novaVantagem(livraria, "Almoço", "0", PNG))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.custo").value("O custo deve ser um número inteiro maior que zero."));
        mvc.perform(novaVantagem(livraria, "Almoço", "abc", PNG))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.custo").value("Valor em formato inválido."));
        mvc.perform(novaVantagem(livraria, "", "10", PNG))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.nome").value("Informe o nome da vantagem."));
    }

    @Test
    @DisplayName("exige uma foto, e que seja imagem de verdade")
    void validaFoto() throws Exception {
        mvc.perform(novaVantagem(livraria, "Almoço", "10", null))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.foto").value("Escolha uma foto."));
        mvc.perform(novaVantagem(livraria, "Almoço", "10", "não sou imagem".getBytes()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.foto").value("Use uma imagem JPG, PNG, WebP ou GIF."));
    }

    @Test
    @DisplayName("só empresa cadastra vantagem")
    void soEmpresaCadastra() throws Exception {
        mvc.perform(novaVantagem(entrar(HELENA, SENHA_PROFESSOR), "Almoço", "10", PNG))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("a empresa remove a própria vantagem, mas não a de outra")
    void remove() throws Exception {
        int id = cadastrarVantagem(livraria, "Almoço com desconto", 120);
        cadastrarEmpresa("45218073000192", "papelaria");

        mvc.perform(comToken(delete("/vantagens/" + id), entrar("papelaria", "segredo123")))
                .andExpect(status().isNotFound());
        mvc.perform(comToken(delete("/vantagens/" + id), livraria))
                .andExpect(status().isNoContent());
        mvc.perform(get("/vantagens")).andExpect(jsonPath("$", hasSize(0)));
    }

}
