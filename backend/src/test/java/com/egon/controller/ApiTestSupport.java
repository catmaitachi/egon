package com.egon.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;

import com.egon.repository.CursoRepository;
import com.jayway.jsonpath.JsonPath;

/**
 * Cadastros e login prontos para os testes das funcionalidades que dependem deles. As subclasses
 * são {@code @Transactional}: tudo o que cada teste cria é desfeito no fim.
 */
abstract class ApiTestSupport {

    /** Professores pré-cadastrados (HU04) com a senha inicial. */
    static final String HELENA = "helena.duarte";
    static final String MARCOS = "marcos.tavares";
    static final String SENHA_PROFESSOR = "professor123";

    /** Instituição da professora Helena e outra, do professor Marcos. */
    static final String HORIZONTE = "11222333000181";
    static final String VALE_VERDE = "45218073000192";

    static final String CPF_ANA = "52998224725";
    static final String CPF_BRUNO = "11144477735";
    static final String CNPJ_LIVRARIA = "12ABC34501DE35";

    /** Os primeiros bytes de um PNG: o bastante para a conferência de formato. */
    static final byte[] PNG = {(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A, 0, 0, 0, 0x0D, 'I', 'H', 'D', 'R'};

    @Autowired MockMvc mvc;
    @Autowired CursoRepository cursos;

    ResultActions cadastrarAluno(String cpf, String login, String instituicao) throws Exception {
        int curso = cursos.findAllByOrderByNomeAsc().getFirst().getId();
        return mvc.perform(post("/alunos").contentType(MediaType.APPLICATION_JSON).content("""
                {"nome": "Aluno %1$s", "email": "%1$s@email.com", "cpf": "%2$s", "rg": "MG-1",
                 "endereco": "Rua A, 1", "cnpjInstituicao": "%3$s", "idCurso": %4$d,
                 "login": "%1$s", "senha": "segredo123"}
                """.formatted(login, cpf, instituicao, curso))).andExpect(status().isCreated());
    }

    ResultActions cadastrarEmpresa(String cnpj, String login) throws Exception {
        return mvc.perform(post("/empresas").contentType(MediaType.APPLICATION_JSON).content("""
                {"nome": "Empresa %1$s", "cnpj": "%2$s", "email": "%1$s@empresa.com",
                 "login": "%1$s", "senha": "segredo123"}
                """.formatted(login, cnpj))).andExpect(status().isCreated());
    }

    /** Token do login; falha o teste se o login não der certo. */
    String entrar(String login, String senha) throws Exception {
        String corpo = mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"login\": \"%s\", \"senha\": \"%s\"}".formatted(login, senha)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(corpo, "$.token");
    }

    String entrarComoAluno(String login) throws Exception {
        return entrar(login, "segredo123");
    }

    static MockHttpServletRequestBuilder comToken(MockHttpServletRequestBuilder requisicao, String token) {
        return requisicao.header(HttpHeaders.AUTHORIZATION, "Bearer " + token);
    }

    ResultActions getComToken(String url, String token) throws Exception {
        return mvc.perform(comToken(get(url), token));
    }

    ResultActions postJson(String url, String token, String json) throws Exception {
        return mvc.perform(comToken(post(url), token).contentType(MediaType.APPLICATION_JSON).content(json));
    }

    ResultActions enviarMoedas(String tokenProfessor, String cpfAluno, int valor, String motivo) throws Exception {
        return postJson("/transacoes", tokenProfessor,
                "{\"cpfAluno\": \"%s\", \"valor\": %d, \"motivo\": \"%s\"}".formatted(cpfAluno, valor, motivo));
    }

    MockMultipartHttpServletRequestBuilder novaVantagem(String token, String nome, String custo, byte[] foto) {
        MockMultipartHttpServletRequestBuilder requisicao = multipart("/vantagens");
        if (foto != null) {
            requisicao.file(new MockMultipartFile("foto", "foto.png", "image/png", foto));
        }
        requisicao.param("nome", nome).param("descricao", "Desconto de 20% no almoço").param("custo", custo)
                .param("tipo", "DESCONTO");
        requisicao.header(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        return requisicao;
    }

    /** Cadastra uma vantagem e devolve o id. */
    int cadastrarVantagem(String tokenEmpresa, String nome, int custo) throws Exception {
        String corpo = mvc.perform(novaVantagem(tokenEmpresa, nome, String.valueOf(custo), PNG))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(corpo, "$.id");
    }

}
