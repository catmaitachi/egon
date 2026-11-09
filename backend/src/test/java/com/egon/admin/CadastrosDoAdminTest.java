package com.egon.admin;

import static com.egon.Logins.comoAdmin;
import static com.egon.Logins.comoProfessor;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

import com.egon.Dados;
import com.egon.repository.AdminRepository;
import com.egon.repository.CotaSemestralRepository;
import com.egon.repository.CursoRepository;
import com.egon.repository.InstituicaoRepository;
import com.egon.repository.ProfessorRepository;

/** O admin cadastra instituições, cursos, professores (HU04) e outros admins. Cada teste é desfeito no fim. */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CadastrosDoAdminTest {

    @Autowired MockMvc mvc;
    @Autowired InstituicaoRepository instituicoes;
    @Autowired CursoRepository cursos;
    @Autowired ProfessorRepository professores;
    @Autowired CotaSemestralRepository cotas;
    @Autowired AdminRepository admins;
    @Autowired PasswordEncoder senhas;

    private ResultActions admin(MockHttpServletRequestBuilder requisicao, String corpo) throws Exception {
        return mvc.perform(requisicao.with(comoAdmin()).contentType(MediaType.APPLICATION_JSON).content(corpo));
    }

    private ResultActions admin(MockHttpServletRequestBuilder requisicao) throws Exception {
        return mvc.perform(requisicao.with(comoAdmin()));
    }

    private UUID horizonte() {
        return instituicoes.findByCnpj(Dados.HORIZONTE).orElseThrow().getId();
    }

    private int curso() {
        return cursos.findAllByOrderByNomeAsc().getFirst().getId();
    }

    private String professor(String cpf, String login, UUID instituicao) {
        return """
                {"cpf": "%s", "nome": "Paula Reis", "email": "%s@horizonte.edu.br", "departamento": "Matemática",
                 "idInstituicao": "%s", "login": "%s", "senha": "inicial123"}
                """.formatted(cpf, login, instituicao, login);
    }

    // ---- Instituições ----

    @Test
    @DisplayName("instituição: cria, edita nome e tipo (o CNPJ fica) e exclui a que não tem ninguém")
    void instituicaoCrud() throws Exception {
        UUID id = Dados.id(admin(post("/instituicoes"), """
                {"cnpj": "73.401.522/0001-79", "nome": "Faculdade Nova", "tipo": "Faculdade"}
                """)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.cnpj").value("73401522000179"))
                .andReturn());

        admin(put("/instituicoes/" + id), """
                {"nome": "Faculdade Nova do Sul", "tipo": "Centro Universitário"}
                """)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Faculdade Nova do Sul"))
                .andExpect(jsonPath("$.cnpj").value("73401522000179"));

        admin(delete("/instituicoes/" + id)).andExpect(status().isNoContent());
        assertThat(instituicoes.existsById(id)).isFalse();
    }

    @Test
    @DisplayName("instituição: recusa CNPJ inválido, repetido e excluir a que tem alunos ou professores")
    void instituicaoRegras() throws Exception {
        admin(post("/instituicoes"), """
                {"cnpj": "73401522000178", "nome": "X", "tipo": "Faculdade"}
                """).andExpect(status().isBadRequest()).andExpect(jsonPath("$.campos.cnpj").exists());
        admin(post("/instituicoes"), """
                {"cnpj": "%s", "nome": "X", "tipo": "Faculdade"}
                """.formatted(Dados.HORIZONTE))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("CNPJ_EM_USO"))
                .andExpect(jsonPath("$.campos.cnpj").exists());
        admin(delete("/instituicoes/" + horizonte()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("INSTITUICAO_EM_USO"));
        admin(delete("/instituicoes/" + UUID.randomUUID())).andExpect(status().isNotFound());
    }

    // ---- Cursos ----

    @Test
    @DisplayName("curso: cria, renomeia, recusa nome repetido e não exclui curso com aluno")
    void cursoCrud() throws Exception {
        int id = com.jayway.jsonpath.JsonPath.read(admin(post("/cursos"), """
                {"nome": "Medicina"}
                """).andExpect(status().isCreated()).andReturn().getResponse().getContentAsString(), "$.id");

        admin(put("/cursos/" + id), """
                {"nome": "Medicina Veterinária"}
                """).andExpect(status().isOk()).andExpect(jsonPath("$.nome").value("Medicina Veterinária"));
        admin(post("/cursos"), """
                {"nome": "medicina veterinária"}
                """).andExpect(status().isConflict()).andExpect(jsonPath("$.codigo").value("CURSO_REPETIDO"));

        Dados.cadastrarAluno(mvc, "Ana Souza", Dados.ANA, "ana", horizonte(), curso());
        admin(delete("/cursos/" + curso()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("CURSO_EM_USO"));
        admin(delete("/cursos/" + id)).andExpect(status().isNoContent());
    }

    // ---- Professores ----

    @Test
    @DisplayName("professor: o admin cadastra e ele já recebe a cota do semestre, com senha em hash")
    void professorNovoGanhaCota() throws Exception {
        UUID id = Dados.id(admin(post("/professores"), professor("529.982.247-25", "paula.reis", horizonte()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.cpf").value(Dados.ANA))
                .andExpect(jsonPath("$.email").value("paula.reis@horizonte.edu.br"))
                .andExpect(jsonPath("$.saldo").value(1000))
                .andExpect(jsonPath("$.instituicao.id").value(horizonte().toString()))
                .andReturn());

        assertThat(cotas.findByProfessorIdOrderByDataDesc(id)).hasSize(1);
        assertThat(senhas.matches("inicial123", professores.findById(id).orElseThrow().getSenha())).isTrue();
        mvc.perform(get("/professores/" + id + "/extrato").with(comoProfessor(id)))
                .andExpect(jsonPath("$.movimentos", hasSize(1)))
                .andExpect(jsonPath("$.movimentos[0].tipo").value("COTA"));
    }

    @Test
    @DisplayName("professor: recusa CPF inválido ou repetido, e-mail inválido ou repetido, login de outro perfil e instituição inexistente")
    void professorRegras() throws Exception {
        admin(post("/professores"), professor("52998224724", "paula", horizonte()))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.campos.cpf").exists());
        admin(post("/professores"), professor(Dados.HELENA, "paula", horizonte()))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.codigo").value("CPF_EM_USO"));
        admin(post("/professores"), professor(Dados.ANA, "admin", horizonte()))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.codigo").value("LOGIN_EM_USO"));
        admin(post("/professores"), professor(Dados.ANA, "paula", UUID.randomUUID()))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.codigo").value("INSTITUICAO_NAO_ENCONTRADA"));
        // O e-mail recebe a confirmação de cada envio (HU07): obrigatório, válido e de um professor só.
        String paula = professor(Dados.ANA, "paula", horizonte());
        admin(post("/professores"), paula.replace("paula@horizonte.edu.br", "helena.duarte@horizonte.edu.br"))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.codigo").value("EMAIL_EM_USO"));
        admin(post("/professores"), paula.replace("paula@horizonte.edu.br", "sem-arroba"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.campos.email").exists());
        admin(post("/professores"), paula.replace("paula@horizonte.edu.br", ""))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.campos.email").exists());
    }

    @Test
    @DisplayName("professor: edita mantendo a senha em branco; exclui o que não enviou, levando as cotas; não exclui quem já enviou")
    void professorEdicaoEExclusao() throws Exception {
        UUID paula = Dados.id(admin(post("/professores"), professor(Dados.ANA, "paula", horizonte())).andReturn());
        String antes = professores.findById(paula).orElseThrow().getSenha();

        admin(put("/professores/" + paula), """
                {"nome": "Paula Reis Lima", "email": "helena.duarte@horizonte.edu.br", "departamento": "Estatística",
                 "idInstituicao": "%s", "login": "paula.lima", "senha": ""}
                """.formatted(horizonte()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("EMAIL_EM_USO"));
        admin(put("/professores/" + paula), """
                {"nome": "Paula Reis Lima", "email": "Paula.Lima@Horizonte.edu.br", "departamento": "Estatística",
                 "idInstituicao": "%s", "login": "paula.lima", "senha": ""}
                """.formatted(horizonte()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("paula.lima@horizonte.edu.br"))
                .andExpect(jsonPath("$.nome").value("Paula Reis Lima"))
                .andExpect(jsonPath("$.login").value("paula.lima"));
        assertThat(professores.findById(paula).orElseThrow().getSenha()).isEqualTo(antes);

        admin(delete("/professores/" + paula)).andExpect(status().isNoContent());
        assertThat(cotas.findByProfessorIdOrderByDataDesc(paula)).isEmpty();

        UUID helena = professores.findByCpf(Dados.HELENA).orElseThrow().getId();
        UUID ana = Dados.cadastrarAluno(mvc, "Ana Souza", Dados.BRUNO, "ana", horizonte(), curso());
        mvc.perform(post("/professores/" + helena + "/transacoes").with(comoProfessor(helena))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"idAluno\": \"%s\", \"valor\": 10, \"motivo\": \"Monitoria\"}".formatted(ana)))
                .andExpect(status().isCreated());
        admin(delete("/professores/" + helena))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("PROFESSOR_COM_TRANSACOES"));
    }

    // ---- Admins ----

    @Test
    @DisplayName("admin: lista, cria, edita e exclui outro admin; não exclui a si mesmo nem o último")
    void adminCrud() throws Exception {
        UUID inicial = admins.findByLogin("admin").orElseThrow().getId();
        admin(get("/admins")).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)));

        UUID outro = Dados.id(admin(post("/admins"), """
                {"nome": "Bia Operações", "login": "bia.ops", "senha": "segredo123"}
                """).andExpect(status().isCreated()).andExpect(jsonPath("$.senha").doesNotExist()).andReturn());
        admin(put("/admins/" + outro), """
                {"nome": "Bia", "login": "bia", "senha": ""}
                """).andExpect(status().isOk()).andExpect(jsonPath("$.login").value("bia"));

        admin(delete("/admins/" + inicial))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("ADMIN_PROTEGIDO"));
        admin(delete("/admins/" + outro)).andExpect(status().isNoContent());
        admin(post("/admins"), """
                {"nome": "X", "login": "helena.duarte", "senha": "segredo123"}
                """).andExpect(status().isConflict()).andExpect(jsonPath("$.codigo").value("LOGIN_EM_USO"));
    }

    @Test
    @DisplayName("só o admin mexe nesses cadastros, mesmo com o corpo válido")
    void soAdmin() throws Exception {
        UUID helena = professores.findByCpf(Dados.HELENA).orElseThrow().getId();
        String[][] casos = {
                {"/instituicoes", "{\"cnpj\": \"73401522000179\", \"nome\": \"X\", \"tipo\": \"Faculdade\"}"},
                {"/cursos", "{\"nome\": \"Medicina\"}"},
                {"/professores", professor(Dados.ANA, "paula", horizonte())},
                {"/admins", "{\"nome\": \"X\", \"login\": \"xis\", \"senha\": \"segredo123\"}"}};
        for (String[] caso : casos) {
            mvc.perform(post(caso[0]).with(comoProfessor(helena)).contentType(MediaType.APPLICATION_JSON).content(caso[1]))
                    .andExpect(status().isForbidden());
        }
        mvc.perform(get("/admins").with(comoProfessor(helena))).andExpect(status().isForbidden());
        mvc.perform(delete("/professores/" + helena).with(comoProfessor(helena))).andExpect(status().isForbidden());
    }

}
