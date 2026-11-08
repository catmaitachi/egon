package com.egon.dev;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import com.egon.Dados;
import com.egon.dto.AlunoCadastroRequest;
import com.egon.repository.CursoRepository;
import com.egon.repository.InstituicaoRepository;
import com.egon.repository.ProfessorRepository;
import com.egon.service.AlunoService;

import jakarta.servlet.http.Cookie;

/**
 * Modo dev: lista as contas e entra sem senha, para testar à mão. Só existe com egon.dev=true;
 * desligado (o padrão), as rotas nem existem. Sem o csrf() do spring-security-test: o token vem
 * do cookie, como no navegador.
 */
class ModoDevTest {

    private static Cookie token(MvcResult resultado, Cookie atual) {
        Cookie ultimo = atual;
        for (Cookie c : resultado.getResponse().getCookies()) {
            if (c.getName().equals("XSRF-TOKEN")) {
                ultimo = c;
            }
        }
        return ultimo;
    }

    @Nested
    @SpringBootTest(properties = "egon.dev=true")
    @AutoConfigureMockMvc
    @Transactional
    class Ligado {

        @Autowired MockMvc mvc;
        @Autowired AlunoService alunos;
        @Autowired InstituicaoRepository instituicoes;
        @Autowired CursoRepository cursos;
        @Autowired ProfessorRepository professores;

        @Test
        @DisplayName("lista as contas dos quatro perfis, sem senha nem documento")
        void listaContas() throws Exception {
            alunos.cadastrar(new AlunoCadastroRequest("Ana Souza", "ana@email.com", Dados.ANA, "1", "Rua A",
                    instituicoes.findByCnpj(Dados.HORIZONTE).orElseThrow().getId(),
                    cursos.findAllByOrderByNomeAsc().getFirst().getId(), "ana", "segredo123"));

            mvc.perform(get("/dev/contas"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[?(@.login == 'ana')].perfil").value("ALUNO"))
                    .andExpect(jsonPath("$[?(@.login == 'helena.duarte')].perfil").value("PROFESSOR"))
                    .andExpect(jsonPath("$[?(@.login == 'admin')].perfil").value("ADMIN"))
                    .andExpect(jsonPath("$[*].senha").value(Matchers.empty()))
                    .andExpect(jsonPath("$[*].cpf").value(Matchers.empty()));
        }

        @Test
        @DisplayName("entra como a conta escolhida, sem senha, e a sessão vale como a de um login normal")
        void entraSemSenha() throws Exception {
            UUID helena = professores.findByCpf(Dados.HELENA).orElseThrow().getId();
            MockHttpSession sessao = new MockHttpSession();
            Cookie cookie = token(mvc.perform(get("/auth/eu").session(sessao)).andReturn(), null);

            MvcResult login = mvc.perform(post("/dev/entrar").session(sessao).cookie(cookie)
                            .header("X-XSRF-TOKEN", cookie.getValue())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"perfil\": \"PROFESSOR\", \"id\": \"%s\"}".formatted(helena)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.perfil").value("PROFESSOR"))
                    .andExpect(jsonPath("$.nome").value("Helena Duarte"))
                    .andReturn();

            mvc.perform(get("/auth/eu").session((MockHttpSession) login.getRequest().getSession()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(helena.toString()));
        }

        @Test
        @DisplayName("conta inexistente é 404; sem token de CSRF é 403")
        void protecoes() throws Exception {
            MockHttpSession sessao = new MockHttpSession();
            Cookie cookie = token(mvc.perform(get("/auth/eu").session(sessao)).andReturn(), null);
            mvc.perform(post("/dev/entrar").session(sessao).cookie(cookie).header("X-XSRF-TOKEN", cookie.getValue())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"perfil\": \"ALUNO\", \"id\": \"%s\"}".formatted(UUID.randomUUID())))
                    .andExpect(status().isNotFound());
            mvc.perform(post("/dev/entrar").session(sessao).contentType(MediaType.APPLICATION_JSON).content("{}"))
                    .andExpect(status().isForbidden());
        }

    }

    @Nested
    @SpringBootTest(properties = "egon.teste=modo-dev-desligado")
    @AutoConfigureMockMvc
    class Desligado {

        @Autowired MockMvc mvc;

        @Test
        @DisplayName("desligado (o padrão), as rotas de dev não existem")
        void naoExiste() throws Exception {
            mvc.perform(get("/dev/contas")).andExpect(status().isNotFound());
            MockHttpSession sessao = new MockHttpSession();
            Cookie cookie = token(mvc.perform(get("/auth/eu").session(sessao)).andReturn(), null);
            mvc.perform(post("/dev/entrar").session(sessao).cookie(cookie).header("X-XSRF-TOKEN", cookie.getValue())
                            .contentType(MediaType.APPLICATION_JSON).content("{\"perfil\": \"ADMIN\", \"id\": \"%s\"}"
                                    .formatted(UUID.randomUUID())))
                    .andExpect(status().isNotFound());
        }

    }

}
