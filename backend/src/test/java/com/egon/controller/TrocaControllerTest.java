package com.egon.controller;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import com.jayway.jsonpath.JsonPath;

/** HU09 e HU10: o aluno troca moedas por uma vantagem; cupom e e-mails para aluno e empresa. */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class TrocaControllerTest extends ApiTestSupport {

    private String ana;
    private String livraria;
    private int almoco;

    @BeforeEach
    void cenario() throws Exception {
        cadastrarAluno(CPF_ANA, "ana", HORIZONTE);
        cadastrarEmpresa(CNPJ_LIVRARIA, "livraria");
        livraria = entrar("livraria", "segredo123");
        almoco = cadastrarVantagem(livraria, "Almoço com desconto", 30);
        enviarMoedas(entrar(HELENA, SENHA_PROFESSOR), CPF_ANA, 50, "Participação").andExpect(status().isCreated());
        ana = entrarComoAluno("ana");
    }

    private ResultActions resgatar(int idVantagem) throws Exception {
        return postJson("/trocas", ana, "{\"idVantagem\": %d}".formatted(idVantagem));
    }

    @Test
    @DisplayName("debita o saldo, gera o cupom e avisa aluno e empresa com o mesmo código")
    void resgata() throws Exception {
        String corpo = resgatar(almoco)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.valor").value(30))
                .andExpect(jsonPath("$.saldoAluno").value(20))
                .andExpect(jsonPath("$.vantagem").value("Almoço com desconto"))
                .andExpect(jsonPath("$.empresa").value("Empresa livraria"))
                .andExpect(jsonPath("$.codigoCupom", matchesPattern("[A-Z2-9]{5}-[A-Z2-9]{5}")))
                .andReturn().getResponse().getContentAsString();
        String codigo = JsonPath.read(corpo, "$.codigoCupom");

        getComToken("/alunos/eu/extrato", ana)
                .andExpect(jsonPath("$.saldo").value(20))
                .andExpect(jsonPath("$.lancamentos[0].tipo").value("SAIDA"))
                .andExpect(jsonPath("$.lancamentos[0].vantagem").value("Almoço com desconto"))
                .andExpect(jsonPath("$.lancamentos[0].empresa").value("Empresa livraria"))
                .andExpect(jsonPath("$.lancamentos[0].codigoCupom").value(codigo));

        getComToken("/notificacoes", ana)
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].codigoCupom").value(codigo))
                .andExpect(jsonPath("$[0].descricao", containsString("Valor debitado: 30 Egos")));

        getComToken("/notificacoes", livraria)
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].codigoCupom").value(codigo))
                .andExpect(jsonPath("$[0].descricao", containsString("Aluno: Aluno ana (ana@email.com)")));

        getComToken("/trocas", livraria)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].codigoCupom").value(codigo))
                .andExpect(jsonPath("$[0].aluno.nome").value("Aluno ana"));
    }

    @Test
    @DisplayName("recusa a troca quando o saldo não cobre o custo, sem debitar nada")
    void saldoInsuficiente() throws Exception {
        resgatar(almoco).andExpect(status().isCreated());

        resgatar(almoco)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("SALDO_INSUFICIENTE"));
        getComToken("/alunos/eu/extrato", ana).andExpect(jsonPath("$.saldo").value(20));
    }

    @Test
    @DisplayName("responde 404 para vantagem inexistente e 403 para quem não é aluno")
    void vantagemInexistenteEPerfil() throws Exception {
        resgatar(999_999).andExpect(status().isNotFound());
        postJson("/trocas", livraria, "{\"idVantagem\": %d}".formatted(almoco)).andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("protege o histórico: vantagem resgatada, aluno com trocas e empresa com resgates não são excluídos")
    void protegeHistorico() throws Exception {
        resgatar(almoco).andExpect(status().isCreated());

        mvc.perform(comToken(delete("/vantagens/" + almoco), livraria))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("VANTAGEM_COM_TROCAS"));
        mvc.perform(delete("/alunos/" + CPF_ANA))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("ALUNO_COM_HISTORICO"));
        mvc.perform(delete("/empresas/" + CNPJ_LIVRARIA))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("EMPRESA_COM_RESGATES"));
    }

}
