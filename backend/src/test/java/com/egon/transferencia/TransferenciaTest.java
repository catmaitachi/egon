package com.egon.transferencia;

import static com.egon.Logins.comoAdmin;
import static com.egon.Logins.comoAluno;
import static com.egon.Logins.comoProfessor;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import com.egon.Dados;
import com.egon.dto.TransacaoRequest;
import com.egon.model.StatusTransferencia;
import com.egon.model.Transferencia;
import com.egon.repository.AlunoRepository;
import com.egon.repository.CarteiraRepository;
import com.egon.repository.CursoRepository;
import com.egon.repository.InstituicaoRepository;
import com.egon.repository.ProfessorRepository;
import com.egon.repository.TransferenciaRepository;
import com.egon.service.TransacaoService;

/**
 * Repasse de Egos entre alunos, sob as regras do professor de origem. Ana tem 100 Egos da Helena;
 * Bruno é colega de curso e instituição; Carla é da mesma instituição e de outro curso; Dani é de
 * outra instituição. Cada teste é desfeito no fim.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class TransferenciaTest {

    @Autowired MockMvc mvc;
    @Autowired CursoRepository cursos;
    @Autowired InstituicaoRepository instituicoes;
    @Autowired ProfessorRepository professores;
    @Autowired AlunoRepository alunos;
    @Autowired CarteiraRepository carteiras;
    @Autowired TransferenciaRepository transferencias;
    @Autowired TransacaoService transacoes;

    private UUID helena;
    private UUID ricardo;
    private UUID ana;
    private UUID bruno;
    private UUID carla;
    private UUID dani;

    @BeforeEach
    void cenario() throws Exception {
        helena = professores.findByCpf(Dados.HELENA).orElseThrow().getId();
        ricardo = professores.findByCpf(Dados.RICARDO).orElseThrow().getId();
        UUID horizonte = instituicoes.findByCnpj(Dados.HORIZONTE).orElseThrow().getId();
        UUID valeVerde = instituicoes.findByCnpj(Dados.VALE_VERDE).orElseThrow().getId();
        int curso = cursos.findAllByOrderByNomeAsc().get(0).getId();
        int outroCurso = cursos.findAllByOrderByNomeAsc().get(1).getId();
        ana = Dados.cadastrarAluno(mvc, "Ana Souza", Dados.ANA, "ana", horizonte, curso);
        bruno = Dados.cadastrarAluno(mvc, "Bruno Lima", Dados.BRUNO, "bruno", horizonte, curso);
        carla = Dados.cadastrarAluno(mvc, "Carla Mendes", Dados.CARLA, "carla", horizonte, outroCurso);
        dani = Dados.cadastrarAluno(mvc, "Dani Rocha", Dados.DANI, "dani", valeVerde, curso);
        transacoes.enviar(helena, new TransacaoRequest(ana, 100, "Monitoria"));
    }

    private ResultActions repassar(UUID de, UUID para, UUID professor, Object valor, String motivo) throws Exception {
        return mvc.perform(post("/alunos/" + de + "/transferencias").with(comoAluno(de))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"destinatarioId": "%s", "professorId": "%s", "valor": %s, "motivo": "%s"}
                        """.formatted(para, professor, valor, motivo)));
    }

    private ResultActions repassar(UUID para, int valor) throws Exception {
        return repassar(ana, para, helena, valor, "Ajudou no trabalho");
    }

    private void regras(String corpo) throws Exception {
        mvc.perform(put("/professores/" + helena + "/regras").with(comoProfessor(helena))
                .contentType(MediaType.APPLICATION_JSON).content(corpo)).andExpect(status().isOk());
    }

    private static final String SEM_APROVACAO = """
            {"permiteRepasse": true, "exigeAprovacao": false, "valorMaximo": null, "limiteMensal": null, "mesmoCurso": false}
            """;

    private int carteira(UUID aluno, UUID professor) {
        return carteiras.findByAlunoIdOrderByProfessorNomeAsc(aluno).stream()
                .filter(c -> c.getProfessor().getId().equals(professor)).mapToInt(c -> c.getSaldo()).sum();
    }

    private ResultActions decidir(UUID transferencia, String acao, org.springframework.test.web.servlet.request.RequestPostProcessor quem)
            throws Exception {
        return mvc.perform(post("/transferencias/" + transferencia + "/" + acao).with(quem));
    }

    // ---- Regras do professor ----

    @Test
    @DisplayName("regras padrão de um professor: repasse permitido, com aprovação, sem limites")
    void regrasPadrao() throws Exception {
        mvc.perform(get("/professores/" + helena + "/regras").with(comoProfessor(helena)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.permiteRepasse").value(true))
                .andExpect(jsonPath("$.exigeAprovacao").value(true))
                .andExpect(jsonPath("$.valorMaximo").doesNotExist())
                .andExpect(jsonPath("$.limiteMensal").doesNotExist())
                .andExpect(jsonPath("$.mesmoCurso").value(false));
        mvc.perform(get("/professores/" + helena + "/regras").with(comoProfessor(ricardo))).andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("regras recusam máximo e limite que não são inteiros positivos")
    void regrasValidadas() throws Exception {
        mvc.perform(put("/professores/" + helena + "/regras").with(comoProfessor(helena))
                        .contentType(MediaType.APPLICATION_JSON).content("""
                                {"permiteRepasse": true, "exigeAprovacao": true, "valorMaximo": 0, "limiteMensal": -5, "mesmoCurso": false}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.valorMaximo").exists())
                .andExpect(jsonPath("$.campos.limiteMensal").exists());
    }

    @Test
    @DisplayName("repasse desligado pelo professor é recusado")
    void repasseDesativado() throws Exception {
        regras("""
                {"permiteRepasse": false, "exigeAprovacao": true, "valorMaximo": null, "limiteMensal": null, "mesmoCurso": false}
                """);
        repassar(bruno, 10).andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.codigo").value("REPASSE_DESATIVADO"))
                .andExpect(jsonPath("$.campos.professorId").exists());
    }

    @Test
    @DisplayName("valor acima do máximo por repasse é recusado no campo valor")
    void acimaDoMaximo() throws Exception {
        regras("""
                {"permiteRepasse": true, "exigeAprovacao": false, "valorMaximo": 50, "limiteMensal": null, "mesmoCurso": false}
                """);
        repassar(bruno, 51).andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.codigo").value("ACIMA_DO_MAXIMO"))
                .andExpect(jsonPath("$.campos.valor").exists());
        repassar(bruno, 50).andExpect(status().isCreated());
    }

    @Test
    @DisplayName("limite mensal conta pendentes e aprovadas do mês; recusadas, canceladas e de meses anteriores não")
    void limiteMensal() throws Exception {
        regras("""
                {"permiteRepasse": true, "exigeAprovacao": true, "valorMaximo": null, "limiteMensal": 100, "mesmoCurso": false}
                """);
        UUID pendente = Dados.id(repassar(bruno, 50).andExpect(status().isCreated()).andReturn());
        UUID recusada = Dados.id(repassar(bruno, 30).andExpect(status().isCreated()).andReturn());
        decidir(recusada, "recusar", comoProfessor(helena)).andExpect(status().isOk());
        transferencias.save(new Transferencia(Instant.now().minus(40, ChronoUnit.DAYS), 40, "Mês passado",
                alunos.findById(ana).orElseThrow(), alunos.findById(bruno).orElseThrow(),
                professores.findById(helena).orElseThrow(), StatusTransferencia.APROVADA));
        decidir(pendente, "aprovar", comoProfessor(helena)).andExpect(status().isOk());

        // No mês: 50 aprovada. Recusada (30) e a do mês passado (40) não contam.
        repassar(bruno, 30).andExpect(status().isCreated());
        repassar(bruno, 21).andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.codigo").value("LIMITE_MENSAL"))
                .andExpect(jsonPath("$.campos.valor").exists());
    }

    @Test
    @DisplayName("com 'mesmo curso' ligado, colega de outro curso é recusado")
    void mesmoCurso() throws Exception {
        regras("""
                {"permiteRepasse": true, "exigeAprovacao": false, "valorMaximo": null, "limiteMensal": null, "mesmoCurso": true}
                """);
        repassar(carla, 10).andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.codigo").value("CURSO_DIFERENTE"))
                .andExpect(jsonPath("$.campos.destinatarioId").exists());
        repassar(bruno, 10).andExpect(status().isCreated());
    }

    // ---- Regras fixas ----

    @Test
    @DisplayName("destinatário precisa existir, ser outra pessoa e ser da instituição do professor de origem")
    void destinatarioInvalido() throws Exception {
        for (UUID destino : List.of(ana, dani, UUID.randomUUID())) {
            repassar(destino, 10).andExpect(status().isUnprocessableContent())
                    .andExpect(jsonPath("$.codigo").value("DESTINATARIO_INVALIDO"))
                    .andExpect(jsonPath("$.campos.destinatarioId").exists());
        }
    }

    @Test
    @DisplayName("não repassa mais do que tem na carteira daquele professor, nem de professor de quem não tem Egos")
    void saldoInsuficiente() throws Exception {
        repassar(bruno, 101).andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.codigo").value("SALDO_INSUFICIENTE"))
                .andExpect(jsonPath("$.campos.valor").exists());
        repassar(ana, bruno, ricardo, 1, "x").andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.codigo").value("SALDO_INSUFICIENTE"));
        assertThat(carteira(ana, helena)).isEqualTo(100);
    }

    @Test
    @DisplayName("motivo obrigatório, valor inteiro positivo")
    void camposObrigatorios() throws Exception {
        repassar(ana, bruno, helena, 10, "  ").andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.motivo").exists());
        for (String valor : new String[] {"0", "-1", "1.5"}) {
            repassar(ana, bruno, helena, valor, "x").andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.campos.valor").exists());
        }
    }

    @Test
    @DisplayName("só o próprio aluno repassa da carteira dele")
    void soOProprio() throws Exception {
        mvc.perform(post("/alunos/" + ana + "/transferencias").with(comoAluno(bruno))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"destinatarioId\": \"%s\", \"professorId\": \"%s\", \"valor\": 10, \"motivo\": \"x\"}"
                                .formatted(bruno, helena)))
                .andExpect(status().isForbidden());
    }

    // ---- Fluxos ----

    @Test
    @DisplayName("sem aprovação: conclui na hora, e os Egos mudam de carteira mantendo a origem")
    void semAprovacao() throws Exception {
        regras(SEM_APROVACAO);

        repassar(bruno, 30).andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("APROVADA"))
                .andExpect(jsonPath("$.professor.id").value(helena.toString()))
                .andExpect(jsonPath("$.remetente.id").value(ana.toString()))
                .andExpect(jsonPath("$.destinatario.nome").value("Bruno Lima"))
                .andExpect(jsonPath("$.dataDecisao").exists());

        assertThat(carteira(ana, helena)).isEqualTo(70);
        assertThat(carteira(bruno, helena)).isEqualTo(30);
    }

    @Test
    @DisplayName("com aprovação: reserva na hora; aprovar credita o colega")
    void aprovar() throws Exception {
        UUID id = Dados.id(repassar(bruno, 30).andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDENTE"))
                .andExpect(jsonPath("$.dataDecisao").doesNotExist())
                .andReturn());
        assertThat(carteira(ana, helena)).isEqualTo(70);
        assertThat(carteira(bruno, helena)).isZero();

        decidir(id, "aprovar", comoProfessor(helena)).andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APROVADA"));
        assertThat(carteira(bruno, helena)).isEqualTo(30);
        assertThat(carteira(ana, helena)).isEqualTo(70);
    }

    @Test
    @DisplayName("recusar (professor) e cancelar (remetente) devolvem os Egos a quem pediu")
    void recusarECancelar() throws Exception {
        UUID recusada = Dados.id(repassar(bruno, 30).andReturn());
        UUID cancelada = Dados.id(repassar(bruno, 20).andReturn());
        assertThat(carteira(ana, helena)).isEqualTo(50);

        decidir(recusada, "recusar", comoProfessor(helena)).andExpect(jsonPath("$.status").value("RECUSADA"));
        decidir(cancelada, "cancelar", comoAluno(ana)).andExpect(jsonPath("$.status").value("CANCELADA"));

        assertThat(carteira(ana, helena)).isEqualTo(100);
        assertThat(carteira(bruno, helena)).isZero();
    }

    @Test
    @DisplayName("só o professor de origem decide; só o remetente cancela; decidir duas vezes é 409")
    void quemDecide() throws Exception {
        UUID id = Dados.id(repassar(bruno, 30).andReturn());

        decidir(id, "aprovar", comoProfessor(ricardo)).andExpect(status().isForbidden());
        decidir(id, "aprovar", comoAluno(ana)).andExpect(status().isForbidden());
        decidir(id, "cancelar", comoAluno(bruno)).andExpect(status().isForbidden());
        decidir(id, "cancelar", comoProfessor(helena)).andExpect(status().isForbidden());
        decidir(id, "aprovar", comoAdmin()).andExpect(status().isForbidden());

        decidir(id, "aprovar", comoProfessor(helena)).andExpect(status().isOk());
        decidir(id, "recusar", comoProfessor(helena)).andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("TRANSFERENCIA_DECIDIDA"));
        decidir(id, "cancelar", comoAluno(ana)).andExpect(status().isConflict());
        decidir(UUID.randomUUID(), "aprovar", comoProfessor(helena)).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("mudar a regra não mexe nas pendentes: continuam decidíveis e não são revalidadas")
    void mudarRegraNaoMexeNasPendentes() throws Exception {
        UUID id = Dados.id(repassar(bruno, 60).andReturn());
        regras("""
                {"permiteRepasse": false, "exigeAprovacao": true, "valorMaximo": 10, "limiteMensal": 10, "mesmoCurso": true}
                """);

        decidir(id, "aprovar", comoProfessor(helena)).andExpect(status().isOk());
        assertThat(carteira(bruno, helena)).isEqualTo(60);
    }

    @Test
    @DisplayName("professor trocado de instituição: os Egos continuam nas carteiras e o repasse segue a instituição atual dele")
    void destinoSegueInstituicaoAtualDoProfessor() throws Exception {
        regras(SEM_APROVACAO);
        UUID valeVerde = instituicoes.findByCnpj(Dados.VALE_VERDE).orElseThrow().getId();
        mvc.perform(put("/professores/" + helena).with(comoAdmin()).contentType(MediaType.APPLICATION_JSON).content("""
                {"nome": "Helena Duarte", "email": "helena.duarte@horizonte.edu.br", "departamento": "Computação",
                 "idInstituicao": "%s", "login": "helena.duarte", "senha": ""}
                """.formatted(valeVerde))).andExpect(status().isOk());

        assertThat(carteira(ana, helena)).isEqualTo(100);
        repassar(bruno, 10).andExpect(jsonPath("$.codigo").value("DESTINATARIO_INVALIDO"));
        repassar(dani, 10).andExpect(status().isCreated());
    }

    // ---- Leituras ----

    @Test
    @DisplayName("extratos mostram as transferências com status e origem; o professor vê as dele com as pendentes primeiro")
    void leituras() throws Exception {
        UUID pendente = Dados.id(repassar(bruno, 30).andReturn());
        UUID aprovada = Dados.id(repassar(bruno, 20).andReturn());
        decidir(aprovada, "aprovar", comoProfessor(helena));

        mvc.perform(get("/alunos/" + ana + "/extrato").with(comoAluno(ana)))
                .andExpect(jsonPath("$.saldo").value(50))
                .andExpect(jsonPath("$.movimentos", hasSize(3)))
                .andExpect(jsonPath("$.movimentos[?(@.id == '%s')].tipo".formatted(pendente)).value("TRANSFERENCIA_ENVIADA"))
                .andExpect(jsonPath("$.movimentos[?(@.id == '%s')].status".formatted(pendente)).value("PENDENTE"))
                .andExpect(jsonPath("$.movimentos[?(@.id == '%s')].origem.nome".formatted(pendente)).value("Helena Duarte"))
                .andExpect(jsonPath("$.movimentos[?(@.id == '%s')].pessoa.nome".formatted(pendente)).value("Bruno Lima"))
                .andExpect(jsonPath("$.carteiras[0].regras.exigeAprovacao").value(true));
        mvc.perform(get("/alunos/" + bruno + "/extrato").with(comoAluno(bruno)))
                .andExpect(jsonPath("$.saldo").value(20))
                .andExpect(jsonPath("$.movimentos[?(@.id == '%s')].tipo".formatted(aprovada)).value("TRANSFERENCIA_RECEBIDA"))
                .andExpect(jsonPath("$.movimentos[?(@.id == '%s')].pessoa.nome".formatted(aprovada)).value("Ana Souza"));
        mvc.perform(get("/professores/" + helena + "/transferencias").with(comoProfessor(helena)))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].status").value("PENDENTE"))
                .andExpect(jsonPath("$[1].status").value("APROVADA"));
        mvc.perform(get("/professores/" + helena + "/transferencias").with(comoProfessor(ricardo)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("colegas e alunos da instituição: só id, nome e curso, sem documentos nem contato")
    void listasDeEscolha() throws Exception {
        mvc.perform(get("/alunos/" + ana + "/colegas").with(comoAluno(ana)))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].nome").value(org.hamcrest.Matchers.contains("Bruno Lima", "Carla Mendes")))
                .andExpect(jsonPath("$[0].curso").exists())
                .andExpect(jsonPath("$[0].cpf").doesNotExist())
                .andExpect(jsonPath("$[0].email").doesNotExist());
        mvc.perform(get("/alunos/" + ana + "/colegas").with(comoAluno(bruno))).andExpect(status().isForbidden());
        mvc.perform(get("/professores/" + helena + "/alunos").with(comoProfessor(helena)))
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].endereco").doesNotExist());
    }

    @Test
    @DisplayName("aluno que recebeu repasse não pode ser excluído")
    void naoExcluiQuemRecebeuRepasse() throws Exception {
        regras(SEM_APROVACAO);
        repassar(bruno, 10).andExpect(status().isCreated());

        mvc.perform(delete("/alunos/" + bruno).with(comoAdmin()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("ALUNO_COM_TRANSACOES"));
    }

}
