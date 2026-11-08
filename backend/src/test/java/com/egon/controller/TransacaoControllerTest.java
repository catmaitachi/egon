package com.egon.controller;

import static com.egon.Logins.comoAdmin;
import static com.egon.Logins.comoAluno;
import static com.egon.Logins.comoProfessor;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import com.egon.Dados;
import com.egon.repository.CursoRepository;
import com.egon.repository.InstituicaoRepository;
import com.egon.repository.ProfessorRepository;
import com.jayway.jsonpath.JsonPath;

/** HU06 (envio), HU11 e HU12 (extratos) pela API. Cada teste é desfeito no fim. */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class TransacaoControllerTest {

    @Autowired MockMvc mvc;
    @Autowired CursoRepository cursos;
    @Autowired InstituicaoRepository instituicoes;
    @Autowired ProfessorRepository professores;

    private UUID helena;
    private UUID ricardo;
    private UUID beatriz;
    private UUID ana;

    @BeforeEach
    void alunaDaHorizonte() throws Exception {
        helena = professor(Dados.HELENA);
        ricardo = professor(Dados.RICARDO);
        beatriz = professor(Dados.BEATRIZ);
        ana = Dados.cadastrarAluno(mvc, "Ana Souza", Dados.ANA, "ana",
                instituicoes.findByCnpj(Dados.HORIZONTE).orElseThrow().getId(),
                cursos.findAllByOrderByNomeAsc().getFirst().getId());
    }

    private UUID professor(String cpf) {
        return professores.findByCpf(cpf).orElseThrow().getId();
    }

    /** {@code valor} entra cru no JSON, para os testes mandarem null ou outro tipo. */
    private ResultActions enviar(UUID professor, Object aluno, String valor, String motivo) throws Exception {
        return mvc.perform(post("/professores/" + professor + "/transacoes")
                .with(comoProfessor(professor))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"idAluno": %s, "valor": %s, "motivo": "%s"}
                        """.formatted(aluno == null ? "null" : "\"" + aluno + "\"", valor, motivo)));
    }

    private int saldoDoProfessor(UUID id) throws Exception {
        String corpo = mvc.perform(get("/professores/" + id).with(comoProfessor(id)))
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(corpo, "$.saldo");
    }

    private int saldoDoAluno(UUID id) throws Exception {
        String corpo = mvc.perform(get("/alunos/" + id).with(comoAluno(id)))
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(corpo, "$.saldo");
    }

    @Test
    @DisplayName("envia moedas: debita o professor, credita o aluno e devolve a transação")
    void envia() throws Exception {
        enviar(helena, ana, "300", "Ótima apresentação do projeto")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(matchesPattern("[0-9a-f-]{36}")))
                .andExpect(jsonPath("$.data").value(matchesPattern("\\d{4}-\\d{2}-\\d{2}T.+Z")))
                .andExpect(jsonPath("$.valor").value(300))
                .andExpect(jsonPath("$.motivo").value("Ótima apresentação do projeto"))
                .andExpect(jsonPath("$.professor.id").value(helena.toString()))
                .andExpect(jsonPath("$.professor.nome").value("Helena Duarte"))
                .andExpect(jsonPath("$.aluno.id").value(ana.toString()))
                .andExpect(jsonPath("$.aluno.nome").value("Ana Souza"))
                .andExpect(jsonPath("$.aluno.cpf").doesNotExist());

        assertThat(saldoDoProfessor(helena)).isEqualTo(700);
        assertThat(saldoDoAluno(ana)).isEqualTo(300);
    }

    @Test
    @DisplayName("a soma dos saldos de professor e aluno não muda, e a transação aparece nos dois extratos")
    void invariante() throws Exception {
        int antes = saldoDoProfessor(helena) + saldoDoAluno(ana);

        String id = Dados.id(enviar(helena, ana, "250", "Monitoria").andReturn()).toString();
        enviar(helena, ana, "120", "Participação").andExpect(status().isCreated());

        assertThat(saldoDoProfessor(helena) + saldoDoAluno(ana)).isEqualTo(antes);
        mvc.perform(get("/alunos/" + ana + "/extrato").with(comoAluno(ana)))
                .andExpect(jsonPath("$.movimentos[1].id").value(id));
        mvc.perform(get("/professores/" + helena + "/extrato").with(comoProfessor(helena)))
                .andExpect(jsonPath("$.movimentos[1].id").value(id));
    }

    @Test
    @DisplayName("aceita gastar o saldo inteiro")
    void gastaTudo() throws Exception {
        enviar(helena, ana, "1000", "Projeto do semestre").andExpect(status().isCreated());

        assertThat(saldoDoProfessor(helena)).isZero();
    }

    @Test
    @DisplayName("recusa valor zero, negativo ou ausente no campo valor")
    void recusaValorInvalido() throws Exception {
        for (String valor : new String[] {"0", "-5", "null"}) {
            enviar(helena, ana, valor, "Motivo")
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.codigo").value("DADOS_INVALIDOS"))
                    .andExpect(jsonPath("$.campos.valor").exists());
        }
    }

    @Test
    @DisplayName("recusa valor com fração ou de outro tipo no campo valor, sem arredondar nem debitar")
    void recusaValorQueNaoEInteiro() throws Exception {
        for (String valor : new String[] {"1.5", "0.9", "\"abc\"", "true"}) {
            enviar(helena, ana, valor, "Motivo")
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.codigo").value("DADOS_INVALIDOS"))
                    .andExpect(jsonPath("$.campos.valor").value("Use um número inteiro."));
        }

        assertThat(saldoDoProfessor(helena)).isEqualTo(1000);
    }

    @Test
    @DisplayName("campo de texto com outro tipo de JSON é recusado nesse campo")
    void recusaMotivoQueNaoETexto() throws Exception {
        mvc.perform(post("/professores/" + helena + "/transacoes")
                        .with(comoProfessor(helena))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"idAluno": "%s", "valor": 10, "motivo": {"texto": "Motivo"}}
                                """.formatted(ana)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("DADOS_INVALIDOS"))
                .andExpect(jsonPath("$.campos.motivo").value("Formato inválido."));
    }

    @Test
    @DisplayName("exige o motivo: em branco é recusado no campo motivo")
    void exigeMotivo() throws Exception {
        enviar(helena, ana, "10", "   ")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.motivo").exists());
    }

    @Test
    @DisplayName("aceita motivo de até 500 caracteres e recusa o de 501")
    void limitaMotivo() throws Exception {
        enviar(helena, ana, "10", "a".repeat(500)).andExpect(status().isCreated());

        enviar(helena, ana, "10", "a".repeat(501))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.motivo").exists());
    }

    @Test
    @DisplayName("exige o aluno no campo idAluno; id que não é UUID também é erro desse campo")
    void exigeAluno() throws Exception {
        enviar(helena, null, "10", "Motivo")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.idAluno").exists());
        enviar(helena, "abc", "10", "Motivo")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.idAluno").exists());
    }

    @Test
    @DisplayName("aluno inexistente é erro do campo idAluno (400), não do recurso")
    void alunoInexistente() throws Exception {
        enviar(helena, UUID.randomUUID(), "10", "Motivo")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("ALUNO_NAO_ENCONTRADO"))
                .andExpect(jsonPath("$.campos.idAluno").exists());
    }

    @Test
    @DisplayName("recusa aluno de outra instituição e não mexe nos saldos")
    void alunoDeOutraInstituicao() throws Exception {
        enviar(beatriz, ana, "10", "Motivo")
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.codigo").value("ALUNO_DE_OUTRA_INSTITUICAO"))
                .andExpect(jsonPath("$.campos.idAluno").exists());

        assertThat(saldoDoProfessor(beatriz)).isEqualTo(1000);
        assertThat(saldoDoAluno(ana)).isZero();
    }

    @Test
    @DisplayName("recusa valor acima do saldo no campo valor, dizendo o saldo disponível")
    void saldoInsuficiente() throws Exception {
        enviar(helena, ana, "1001", "Motivo")
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.codigo").value("SALDO_INSUFICIENTE"))
                .andExpect(jsonPath("$.campos.valor").value(containsString("1000")));

        assertThat(saldoDoProfessor(helena)).isEqualTo(1000);
        assertThat(saldoDoAluno(ana)).isZero();
    }

    @Test
    @DisplayName("professor que não existe mais não envia: a sessão dele não vale (401)")
    void professorInexistente() throws Exception {
        enviar(UUID.randomUUID(), ana, "10", "Motivo")
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.codigo").value("NAO_AUTENTICADO"));
    }

    @Test
    @DisplayName("extrato do aluno: saldo, carteiras por professor e recebimentos, do mais recente para o mais antigo")
    void extratoDoAluno() throws Exception {
        enviar(helena, ana, "100", "Primeiro").andExpect(status().isCreated());
        enviar(ricardo, ana, "200", "Segundo").andExpect(status().isCreated());

        mvc.perform(get("/alunos/" + ana + "/extrato").with(comoAluno(ana)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.saldo").value(300))
                .andExpect(jsonPath("$.carteiras", hasSize(2)))
                .andExpect(jsonPath("$.carteiras[0].professor.nome").value("Helena Duarte"))
                .andExpect(jsonPath("$.carteiras[0].saldo").value(100))
                .andExpect(jsonPath("$.carteiras[1].professor.id").value(ricardo.toString()))
                .andExpect(jsonPath("$.carteiras[1].saldo").value(200))
                .andExpect(jsonPath("$.movimentos", hasSize(2)))
                .andExpect(jsonPath("$.movimentos[0].tipo").value("RECEBIMENTO"))
                .andExpect(jsonPath("$.movimentos[0].id").exists())
                .andExpect(jsonPath("$.movimentos[0].data").exists())
                .andExpect(jsonPath("$.movimentos[0].valor").value(200))
                .andExpect(jsonPath("$.movimentos[0].motivo").value("Segundo"))
                .andExpect(jsonPath("$.movimentos[0].pessoa.id").value(ricardo.toString()))
                .andExpect(jsonPath("$.movimentos[0].pessoa.nome").value("Ricardo Moraes"))
                .andExpect(jsonPath("$.movimentos[0].semestre").value(nullValue()))
                .andExpect(jsonPath("$.movimentos[1].valor").value(100))
                .andExpect(jsonPath("$.movimentos[1].pessoa.id").value(helena.toString()));
    }

    @Test
    @DisplayName("extrato do professor: envios e a cota do semestre, do mais recente para o mais antigo")
    void extratoDoProfessor() throws Exception {
        enviar(helena, ana, "100", "Monitoria").andExpect(status().isCreated());

        mvc.perform(get("/professores/" + helena + "/extrato").with(comoProfessor(helena)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.saldo").value(900))
                .andExpect(jsonPath("$.carteiras", hasSize(0)))
                .andExpect(jsonPath("$.movimentos", hasSize(2)))
                .andExpect(jsonPath("$.movimentos[0].tipo").value("ENVIO"))
                .andExpect(jsonPath("$.movimentos[0].valor").value(100))
                .andExpect(jsonPath("$.movimentos[0].motivo").value("Monitoria"))
                .andExpect(jsonPath("$.movimentos[0].pessoa.id").value(ana.toString()))
                .andExpect(jsonPath("$.movimentos[0].pessoa.nome").value("Ana Souza"))
                .andExpect(jsonPath("$.movimentos[0].semestre").value(nullValue()))
                .andExpect(jsonPath("$.movimentos[1].tipo").value("COTA"))
                .andExpect(jsonPath("$.movimentos[1].id").exists())
                .andExpect(jsonPath("$.movimentos[1].data").exists())
                .andExpect(jsonPath("$.movimentos[1].valor").value(1000))
                .andExpect(jsonPath("$.movimentos[1].motivo").value(nullValue()))
                .andExpect(jsonPath("$.movimentos[1].pessoa").value(nullValue()))
                .andExpect(jsonPath("$.movimentos[1].semestre").value(matchesPattern("\\d{4}\\.[12]")));
    }

    @Test
    @DisplayName("extrato de id inexistente responde 404 com o código do perfil")
    void extratoInexistente() throws Exception {
        UUID ninguem = UUID.randomUUID();
        mvc.perform(get("/alunos/" + ninguem + "/extrato").with(comoAdmin()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value("ALUNO_NAO_ENCONTRADO"));
        mvc.perform(get("/professores/" + ninguem + "/extrato").with(comoAdmin()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value("PROFESSOR_NAO_ENCONTRADO"));
    }

    @Test
    @DisplayName("não exclui aluno que já recebeu moedas: o histórico do professor depende dele")
    void naoExcluiAlunoComTransacoes() throws Exception {
        enviar(helena, ana, "10", "Motivo").andExpect(status().isCreated());

        mvc.perform(delete("/alunos/" + ana).with(comoAdmin()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("ALUNO_COM_TRANSACOES"));
        mvc.perform(get("/alunos/" + ana).with(comoAdmin())).andExpect(status().isOk());
    }

}
