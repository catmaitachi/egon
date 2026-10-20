package com.egon.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

/** HU01: um login para os três perfis. */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthControllerTest extends ApiTestSupport {

    private ResultActions login(String login, String senha) throws Exception {
        return mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"login\": \"%s\", \"senha\": \"%s\"}".formatted(login, senha)));
    }

    @Test
    @DisplayName("aluno entra e o token identifica o perfil dele")
    void alunoEntra() throws Exception {
        cadastrarAluno(CPF_ANA, "ana", HORIZONTE);

        login("ana", "segredo123")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.perfil").value("ALUNO"))
                .andExpect(jsonPath("$.id").value(CPF_ANA))
                .andExpect(jsonPath("$.nome").value("Aluno ana"))
                .andExpect(jsonPath("$.token").isNotEmpty());

        getComToken("/auth/eu", entrarComoAluno("ana"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.perfil").value("ALUNO"))
                .andExpect(jsonPath("$.token").doesNotExist());
    }

    @Test
    @DisplayName("professor pré-cadastrado entra com a senha inicial (HU04), em maiúsculas ou não")
    void professorEntra() throws Exception {
        login("Helena.Duarte", SENHA_PROFESSOR)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.perfil").value("PROFESSOR"))
                .andExpect(jsonPath("$.nome").value("Helena Duarte"));
    }

    @Test
    @DisplayName("empresa entra")
    void empresaEntra() throws Exception {
        cadastrarEmpresa(CNPJ_LIVRARIA, "livraria");

        login("livraria", "segredo123")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.perfil").value("EMPRESA"))
                .andExpect(jsonPath("$.id").value(CNPJ_LIVRARIA));
    }

    @Test
    @DisplayName("senha errada e login inexistente recebem o mesmo aviso, sem dizer qual errou")
    void credenciaisInvalidas() throws Exception {
        cadastrarAluno(CPF_ANA, "ana", HORIZONTE);

        login("ana", "senhaErrada1")
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.codigo").value("CREDENCIAIS_INVALIDAS"))
                .andExpect(jsonPath("$.mensagem").value("Login ou senha incorretos."));
        login("ninguem", "segredo123")
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensagem").value("Login ou senha incorretos."));
    }

    @Test
    @DisplayName("sem token, ou com token adulterado, a resposta é 401")
    void exigeToken() throws Exception {
        mvc.perform(get("/auth/eu"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.codigo").value("NAO_AUTENTICADO"));

        String token = entrar(HELENA, SENHA_PROFESSOR);
        String adulterado = token.substring(0, token.length() - 2) + (token.endsWith("A") ? "BB" : "AA");
        getComToken("/auth/eu", adulterado).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("token de cadastro excluído deixa de valer")
    void cadastroExcluido() throws Exception {
        cadastrarAluno(CPF_ANA, "ana", HORIZONTE);
        String token = entrarComoAluno("ana");

        mvc.perform(delete("/alunos/" + CPF_ANA))
                .andExpect(status().isNoContent());

        getComToken("/auth/eu", token).andExpect(status().isUnauthorized());
    }

}
