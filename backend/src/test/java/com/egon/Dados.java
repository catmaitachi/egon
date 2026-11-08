package com.egon;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.jayway.jsonpath.JsonPath;

/** Dados da carga inicial e atalhos para criar cadastros pela API nos testes. */
public final class Dados {

    public static final String HORIZONTE = "11222333000181";
    public static final String VALE_VERDE = "45218073000192";
    public static final String AURORA = "28039156000103";

    /** Professores pré-cadastrados: Helena e Ricardo na Horizonte, Beatriz no Vale Verde, Marcelo na Aurora. */
    public static final String HELENA = "48291573646";
    public static final String RICARDO = "61528394755";
    public static final String BEATRIZ = "73946125891";
    public static final String MARCELO = "85713462919";

    /** CPFs válidos livres para alunos de teste. */
    public static final String ANA = "52998224725";
    public static final String BRUNO = "11144477735";
    public static final String CARLA = "39053344705";
    public static final String DANI = "52601815906";
    public static final String EDU = "08301661305";

    private Dados() {
    }

    public static UUID id(MvcResult resultado) throws Exception {
        return UUID.fromString(JsonPath.read(resultado.getResponse().getContentAsString(), "$.id"));
    }

    /** Cadastro público de aluno; devolve o id. */
    public static UUID cadastrarAluno(MockMvc mvc, String nome, String cpf, String login, UUID instituicao, int curso)
            throws Exception {
        return id(mvc.perform(post("/alunos").with(Logins.anonimo()).contentType(MediaType.APPLICATION_JSON).content("""
                {"nome": "%s", "email": "%s@email.com", "cpf": "%s", "rg": "MG-12.345.678",
                 "endereco": "Rua das Flores, 10", "idInstituicao": "%s", "idCurso": %d,
                 "login": "%s", "senha": "segredo123"}
                """.formatted(nome, login, cpf, instituicao, curso, login)))
                .andExpect(status().isCreated())
                .andReturn());
    }

    /** Imagem PNG mínima (só a assinatura e o começo do cabeçalho) para as fotos de vantagem. */
    public static final byte[] PNG = {(byte) 0x89, 'P', 'N', 'G', '\r', '\n', 0x1A, '\n', 0, 0, 0, 13, 'I', 'H', 'D', 'R'};

    /** Vantagem cadastrada pela própria empresa, com foto; devolve o id. */
    public static UUID cadastrarVantagem(MockMvc mvc, UUID empresa, String nome, int custo) throws Exception {
        return id(mvc.perform(multipart("/empresas/" + empresa + "/vantagens")
                        .file(new MockMultipartFile("foto", "foto.png", "image/png", PNG))
                        .param("nome", nome).param("descricao", "Descrição de " + nome)
                        .param("custo", String.valueOf(custo)).param("tipo", "PRODUTO")
                        .with(Logins.comoEmpresa(empresa)))
                .andExpect(status().isCreated())
                .andReturn());
    }

    /** Cadastro público de empresa; devolve o id. */
    public static UUID cadastrarEmpresa(MockMvc mvc, String nome, String cnpj, String login) throws Exception {
        return id(mvc.perform(post("/empresas").with(Logins.anonimo()).contentType(MediaType.APPLICATION_JSON).content("""
                {"nome": "%s", "cnpj": "%s", "email": "%s@empresa.com", "login": "%s", "senha": "segredo123"}
                """.formatted(nome, cnpj, login, login)))
                .andExpect(status().isCreated())
                .andReturn());
    }

}
