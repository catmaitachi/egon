package com.egon.controller;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.transaction.annotation.Transactional;

/** HU06, HU07, HU11 e HU12: o professor reconhece, o aluno recebe e os dois veem no extrato. */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class TransacaoControllerTest extends ApiTestSupport {

    private String helena;

    @BeforeEach
    void cenario() throws Exception {
        // Ana estuda na instituição da Helena; Bruno, na do Marcos.
        cadastrarAluno(CPF_ANA, "ana", HORIZONTE);
        cadastrarAluno(CPF_BRUNO, "bruno", VALE_VERDE);
        helena = entrar(HELENA, SENHA_PROFESSOR);
    }

    @Test
    @DisplayName("debita o professor, credita o aluno e registra o e-mail com professor, valor e mensagem")
    void enviaMoedas() throws Exception {
        enviarMoedas(helena, CPF_ANA, 50, "Ótima apresentação do seminário")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.valor").value(50))
                .andExpect(jsonPath("$.aluno").value("Aluno ana"))
                .andExpect(jsonPath("$.saldoProfessor").value(950));

        String ana = entrarComoAluno("ana");
        getComToken("/alunos/eu/extrato", ana)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.saldo").value(50))
                .andExpect(jsonPath("$.lancamentos", hasSize(1)))
                .andExpect(jsonPath("$.lancamentos[0].tipo").value("ENTRADA"))
                .andExpect(jsonPath("$.lancamentos[0].pessoa").value("Helena Duarte"))
                .andExpect(jsonPath("$.lancamentos[0].motivo").value("Ótima apresentação do seminário"));

        getComToken("/notificacoes", ana)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].assunto").value("Você recebeu 50 Egos de Helena Duarte"))
                .andExpect(jsonPath("$[0].descricao", containsString("Ótima apresentação do seminário")))
                .andExpect(jsonPath("$[0].descricao", containsString("Seu saldo agora é de 50 Egos.")));
    }

    @Test
    @DisplayName("extrato do professor: cota recebida e envios, com o saldo que sobrou (HU12)")
    void extratoDoProfessor() throws Exception {
        enviarMoedas(helena, CPF_ANA, 120, "Ajudou os colegas no laboratório").andExpect(status().isCreated());

        getComToken("/professores/eu/extrato", helena)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.saldo").value(880))
                .andExpect(jsonPath("$.lancamentos", hasSize(2)))
                .andExpect(jsonPath("$.lancamentos[0].tipo").value("SAIDA"))
                .andExpect(jsonPath("$.lancamentos[0].pessoa").value("Aluno ana"))
                .andExpect(jsonPath("$.lancamentos[0].valor").value(120))
                .andExpect(jsonPath("$.lancamentos[1].origem").value("COTA"))
                .andExpect(jsonPath("$.lancamentos[1].valor").value(1000));
    }

    @Test
    @DisplayName("bloqueia envio maior que o saldo, sem mexer em nenhum saldo")
    void saldoInsuficiente() throws Exception {
        enviarMoedas(helena, CPF_ANA, 1001, "Excelente")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("SALDO_INSUFICIENTE"))
                .andExpect(jsonPath("$.campos.valor").exists());

        getComToken("/professores/eu", helena).andExpect(jsonPath("$.saldo").value(1000));
    }

    @Test
    @DisplayName("exige valor positivo e justificativa")
    void validaValorEMotivo() throws Exception {
        enviarMoedas(helena, CPF_ANA, 0, "Excelente")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.valor").value("Envie pelo menos 1 moeda."));
        enviarMoedas(helena, CPF_ANA, 10, "   ")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.motivo").value("Escreva o motivo do reconhecimento."));
    }

    @Test
    @DisplayName("só aceita alunos da instituição do professor")
    void alunoDeOutraInstituicao() throws Exception {
        enviarMoedas(helena, CPF_BRUNO, 10, "Excelente")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("ALUNO_DE_OUTRA_INSTITUICAO"));
        enviarMoedas(helena, "39053344705", 10, "Excelente")
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("a busca do professor só traz alunos da instituição dele")
    void buscaAlunos() throws Exception {
        getComToken("/professores/eu/alunos?busca=aluno", helena)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].cpf").value(CPF_ANA));
    }

    @Test
    @DisplayName("só professor envia moedas")
    void soProfessorEnvia() throws Exception {
        enviarMoedas(entrarComoAluno("ana"), CPF_ANA, 10, "Para mim mesma")
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.codigo").value("ACESSO_NEGADO"));
    }

}
