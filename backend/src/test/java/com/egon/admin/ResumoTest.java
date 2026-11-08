package com.egon.admin;

import static com.egon.Logins.comoAdmin;
import static com.egon.Logins.comoAluno;
import static com.egon.Logins.comoProfessor;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.YearMonth;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.egon.Dados;
import com.egon.model.CotaSemestral;
import com.egon.model.Professor;
import com.egon.repository.CarteiraRepository;
import com.egon.repository.CotaSemestralRepository;
import com.egon.repository.CursoRepository;
import com.egon.repository.InstituicaoRepository;
import com.egon.repository.ProfessorRepository;
import com.egon.repository.TransferenciaRepository;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;

/** O resumo do admin e a conta dos Egos. Cada teste é desfeito no fim. */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ResumoTest {

    @Autowired MockMvc mvc;
    @Autowired ProfessorRepository professores;
    @Autowired InstituicaoRepository instituicoes;
    @Autowired CursoRepository cursos;
    @Autowired CotaSemestralRepository cotas;
    @Autowired CarteiraRepository carteiras;
    @Autowired TransferenciaRepository transferencias;

    private DocumentContext resumo() throws Exception {
        return JsonPath.parse(mvc.perform(get("/admin/resumo").with(comoAdmin()))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString());
    }

    private void post(String caminho, org.springframework.test.web.servlet.request.RequestPostProcessor quem, String corpo)
            throws Exception {
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post(caminho).with(quem)
                .contentType(MediaType.APPLICATION_JSON).content(corpo)).andExpect(status().is2xxSuccessful());
    }

    private UUID postId(String caminho, org.springframework.test.web.servlet.request.RequestPostProcessor quem, String corpo)
            throws Exception {
        return Dados.id(mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post(caminho)
                .with(quem).contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().is2xxSuccessful()).andReturn());
    }

    @Test
    @DisplayName("invariante: Egos emitidos pela cota = com professores + nas carteiras + reservados + resgatados")
    void invarianteDosEgos() throws Exception {
        UUID horizonte = instituicoes.findByCnpj(Dados.HORIZONTE).orElseThrow().getId();
        int curso = cursos.findAllByOrderByNomeAsc().getFirst().getId();
        UUID helena = professores.findByCpf(Dados.HELENA).orElseThrow().getId();
        UUID ricardo = professores.findByCpf(Dados.RICARDO).orElseThrow().getId();
        UUID ana = Dados.cadastrarAluno(mvc, "Ana Souza", Dados.ANA, "ana", horizonte, curso);
        UUID bruno = Dados.cadastrarAluno(mvc, "Bruno Lima", Dados.BRUNO, "bruno", horizonte, curso);
        UUID paula = postId("/professores", comoAdmin(), """
                {"cpf": "%s", "nome": "Paula Reis", "departamento": "Matemática", "idInstituicao": "%s",
                 "login": "paula", "senha": "inicial123"}
                """.formatted(Dados.EDU, horizonte));

        String envio = "{\"idAluno\": \"%s\", \"valor\": %d, \"motivo\": \"m\"}";
        post("/professores/" + helena + "/transacoes", comoProfessor(helena), envio.formatted(ana, 200));
        post("/professores/" + ricardo + "/transacoes", comoProfessor(ricardo), envio.formatted(ana, 50));
        post("/professores/" + paula + "/transacoes", comoProfessor(paula), envio.formatted(bruno, 10));
        String repasse = "{\"destinatarioId\": \"%s\", \"professorId\": \"%s\", \"valor\": %d, \"motivo\": \"r\"}";
        UUID pendente = postId("/alunos/" + ana + "/transferencias", comoAluno(ana), repasse.formatted(bruno, helena, 40));
        UUID aprovada = postId("/alunos/" + ana + "/transferencias", comoAluno(ana), repasse.formatted(bruno, helena, 30));
        UUID recusada = postId("/alunos/" + ana + "/transferencias", comoAluno(ana), repasse.formatted(bruno, ricardo, 20));
        UUID cancelada = postId("/alunos/" + ana + "/transferencias", comoAluno(ana), repasse.formatted(bruno, helena, 10));
        post("/transferencias/" + aprovada + "/aprovar", comoProfessor(helena), "");
        post("/transferencias/" + recusada + "/recusar", comoProfessor(ricardo), "");
        post("/transferencias/" + cancelada + "/cancelar", comoAluno(ana), "");
        UUID livraria = Dados.cadastrarEmpresa(mvc, "Livraria", "45218073000192", "livraria");
        UUID livro = Dados.cadastrarVantagem(mvc, livraria, "Livro", 20);
        post("/alunos/" + ana + "/resgates", comoAluno(ana), "{\"vantagemId\": \"%s\"}".formatted(livro));

        DocumentContext r = resumo();
        long emitidos = cotas.findAll().stream().mapToLong(CotaSemestral::getValor).sum();
        long comProfessores = professores.findAll().stream().mapToLong(Professor::getSaldo).sum();
        long comAlunos = carteiras.findAll().stream().mapToLong(c -> c.getSaldo()).sum();
        assertThat(r.read("$.egos.emitidos", Long.class)).isEqualTo(emitidos).isEqualTo(5000);
        assertThat(r.read("$.egos.comProfessores", Long.class)).isEqualTo(comProfessores);
        assertThat(r.read("$.egos.comAlunos", Long.class)).isEqualTo(comAlunos).isEqualTo(200);
        assertThat(r.read("$.egos.resgatados", Long.class)).isEqualTo(20);
        assertThat(r.read("$.egos.reservados", Long.class)).isEqualTo(40);
        assertThat(r.read("$.egos.emitidos", Long.class)).isEqualTo(
                r.read("$.egos.comProfessores", Long.class) + r.read("$.egos.comAlunos", Long.class)
                        + r.read("$.egos.reservados", Long.class) + r.read("$.egos.resgatados", Long.class));
        assertThat(pendente).isNotNull();
    }

    @Test
    @DisplayName("envios por mês: os 6 últimos meses, com zero onde não houve envio, e o atual por último")
    void enviosPorMes() throws Exception {
        UUID horizonte = instituicoes.findByCnpj(Dados.HORIZONTE).orElseThrow().getId();
        UUID helena = professores.findByCpf(Dados.HELENA).orElseThrow().getId();
        UUID ana = Dados.cadastrarAluno(mvc, "Ana Souza", Dados.ANA, "ana", horizonte,
                cursos.findAllByOrderByNomeAsc().getFirst().getId());
        post("/professores/" + helena + "/transacoes", comoProfessor(helena),
                "{\"idAluno\": \"%s\", \"valor\": 70, \"motivo\": \"m\"}".formatted(ana));

        DocumentContext r = resumo();
        List<Map<String, Object>> meses = r.read("$.enviosPorMes");
        assertThat(meses).hasSize(6);
        String atual = YearMonth.now(ZoneId.of("America/Sao_Paulo")).toString();
        assertThat(meses.getLast()).containsEntry("mes", atual).containsEntry("quantidade", 1).containsEntry("valor", 70);
        assertThat(meses.getFirst()).containsEntry("quantidade", 0).containsEntry("valor", 0);
        assertThat(r.read("$.porInstituicao[?(@.nome == 'Universidade Horizonte')].enviado", List.class)).containsExactly(70);
        assertThat(r.read("$.porInstituicao[?(@.nome == 'Universidade Horizonte')].alunos", List.class)).containsExactly(1);
        assertThat(r.read("$.porInstituicao[?(@.nome == 'Universidade Horizonte')].professores", List.class)).containsExactly(2);
        assertThat(r.read("$.contagens.instituicoes", Integer.class)).isEqualTo(3);
        assertThat(r.read("$.contagens.professores", Integer.class)).isEqualTo(4);
        assertThat(r.read("$.contagens.alunos", Integer.class)).isEqualTo(1);
        assertThat(r.read("$.contagens.cursos", Integer.class)).isEqualTo(8);
    }

    @Test
    @DisplayName("só o admin vê o resumo")
    void soAdmin() throws Exception {
        UUID helena = professores.findByCpf(Dados.HELENA).orElseThrow().getId();
        mvc.perform(get("/admin/resumo").with(comoProfessor(helena))).andExpect(status().isForbidden());
        mvc.perform(get("/admin/resumo")).andExpect(status().isUnauthorized());
    }

}
