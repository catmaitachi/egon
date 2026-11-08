package com.egon.security;

import static com.egon.Logins.anonimo;
import static com.egon.Logins.comoAdmin;
import static com.egon.Logins.comoAluno;
import static com.egon.Logins.comoEmpresa;
import static com.egon.Logins.comoProfessor;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;

import com.egon.Dados;
import com.egon.repository.CursoRepository;
import com.egon.repository.InstituicaoRepository;
import com.egon.repository.ProfessorRepository;

/**
 * A matriz de acesso da spec: anônimo → 401, perfil errado ou outra pessoa → 403, quem pode → 2xx.
 * Cada teste é desfeito no fim.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AcessoTest {

    @Autowired MockMvc mvc;
    @Autowired CursoRepository cursos;
    @Autowired InstituicaoRepository instituicoes;
    @Autowired ProfessorRepository professores;

    private UUID ana;
    private UUID bruno;
    private UUID helena;
    private UUID ricardo;
    private UUID livraria;
    private UUID papelaria;
    private int curso;
    private UUID horizonte;

    @BeforeEach
    void cadastros() throws Exception {
        curso = cursos.findAllByOrderByNomeAsc().getFirst().getId();
        horizonte = instituicoes.findByCnpj(Dados.HORIZONTE).orElseThrow().getId();
        ana = Dados.cadastrarAluno(mvc, "Ana Souza", Dados.ANA, "ana", horizonte, curso);
        bruno = Dados.cadastrarAluno(mvc, "Bruno Lima", Dados.BRUNO, "bruno", horizonte, curso);
        helena = professores.findByCpf(Dados.HELENA).orElseThrow().getId();
        ricardo = professores.findByCpf(Dados.RICARDO).orElseThrow().getId();
        livraria = Dados.cadastrarEmpresa(mvc, "Livraria", "45218073000192", "livraria");
        papelaria = Dados.cadastrarEmpresa(mvc, "Papelaria", "28039156000103", "papelaria");
    }

    private void espera(MockHttpServletRequestBuilder requisicao, RequestPostProcessor quem, int status) throws Exception {
        mvc.perform(requisicao.with(quem)).andExpect(status().is(status));
    }

    private void semLogin(MockHttpServletRequestBuilder requisicao) throws Exception {
        mvc.perform(requisicao.with(anonimo()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.codigo").value("NAO_AUTENTICADO"));
    }

    private void negado(MockHttpServletRequestBuilder requisicao, RequestPostProcessor quem) throws Exception {
        mvc.perform(requisicao.with(quem))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.codigo").value("ACESSO_NEGADO"));
    }

    private MockHttpServletRequestBuilder json(MockHttpServletRequestBuilder requisicao, String corpo) {
        return requisicao.contentType(MediaType.APPLICATION_JSON).content(corpo);
    }

    private String edicaoAluno(String nome, String login) {
        return """
                {"nome": "%s", "email": "%s@email.com", "rg": "1", "endereco": "Rua A", "idInstituicao": "%s",
                 "idCurso": %d, "login": "%s", "senha": ""}
                """.formatted(nome, login, horizonte, curso, login);
    }

    @Test
    @DisplayName("públicos: listas do cadastro e os dois cadastros, sem login")
    void publicos() throws Exception {
        espera(get("/instituicoes"), anonimo(), 200);
        espera(get("/cursos"), anonimo(), 200);
    }

    @Test
    @DisplayName("lista de alunos: só admin")
    void listaDeAlunos() throws Exception {
        semLogin(get("/alunos"));
        negado(get("/alunos"), comoAluno(ana));
        negado(get("/alunos"), comoProfessor(helena));
        negado(get("/alunos"), comoEmpresa(livraria));
        espera(get("/alunos"), comoAdmin(), 200);
    }

    @Test
    @DisplayName("dados e extrato do aluno: o próprio e o admin")
    void dadosDoAluno() throws Exception {
        for (String caminho : new String[] {"/alunos/" + ana, "/alunos/" + ana + "/extrato"}) {
            semLogin(get(caminho));
            negado(get(caminho), comoAluno(bruno));
            negado(get(caminho), comoProfessor(helena));
            negado(get(caminho), comoEmpresa(livraria));
            espera(get(caminho), comoAluno(ana), 200);
            espera(get(caminho), comoAdmin(), 200);
        }
    }

    @Test
    @DisplayName("editar aluno: o próprio e o admin; excluir: só o admin")
    void edicaoEExclusaoDeAluno() throws Exception {
        negado(json(put("/alunos/" + ana), edicaoAluno("Ana", "ana")), comoAluno(bruno));
        espera(json(put("/alunos/" + ana), edicaoAluno("Ana", "ana")), comoAluno(ana), 200);
        espera(json(put("/alunos/" + ana), edicaoAluno("Ana", "ana")), comoAdmin(), 200);
        negado(delete("/alunos/" + bruno), comoAluno(bruno));
        espera(delete("/alunos/" + bruno), comoAdmin(), 204);
    }

    @Test
    @DisplayName("lista de professores: só admin; dados e extrato: o próprio e o admin")
    void professores() throws Exception {
        semLogin(get("/professores"));
        negado(get("/professores"), comoProfessor(helena));
        negado(get("/professores"), comoAluno(ana));
        espera(get("/professores"), comoAdmin(), 200);
        for (String caminho : new String[] {"/professores/" + helena, "/professores/" + helena + "/extrato"}) {
            negado(get(caminho), comoProfessor(ricardo));
            negado(get(caminho), comoAluno(ana));
            espera(get(caminho), comoProfessor(helena), 200);
            espera(get(caminho), comoAdmin(), 200);
        }
    }

    @Test
    @DisplayName("enviar Egos: só o próprio professor, nem o admin")
    void envio() throws Exception {
        String corpo = """
                {"idAluno": "%s", "valor": 10, "motivo": "Monitoria"}
                """.formatted(ana);
        semLogin(json(post("/professores/" + helena + "/transacoes"), corpo));
        negado(json(post("/professores/" + helena + "/transacoes"), corpo), comoProfessor(ricardo));
        negado(json(post("/professores/" + helena + "/transacoes"), corpo), comoAdmin());
        negado(json(post("/professores/" + helena + "/transacoes"), corpo), comoAluno(ana));
        espera(json(post("/professores/" + helena + "/transacoes"), corpo), comoProfessor(helena), 201);
    }

    @Test
    @DisplayName("empresas: lista e exclusão só do admin; dados e edição, a própria e o admin")
    void empresas() throws Exception {
        String edicao = """
                {"nome": "Livraria Central", "email": "livraria@empresa.com", "login": "livraria", "senha": ""}
                """;
        semLogin(get("/empresas"));
        negado(get("/empresas"), comoEmpresa(livraria));
        espera(get("/empresas"), comoAdmin(), 200);
        negado(get("/empresas/" + livraria), comoEmpresa(papelaria));
        negado(get("/empresas/" + livraria), comoAluno(ana));
        espera(get("/empresas/" + livraria), comoEmpresa(livraria), 200);
        espera(get("/empresas/" + livraria), comoAdmin(), 200);
        negado(json(put("/empresas/" + livraria), edicao), comoEmpresa(papelaria));
        espera(json(put("/empresas/" + livraria), edicao), comoEmpresa(livraria), 200);
        negado(delete("/empresas/" + papelaria), comoEmpresa(papelaria));
        espera(delete("/empresas/" + papelaria), comoAdmin(), 204);
    }

}
