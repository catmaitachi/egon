package com.egon.resgate;

import static com.egon.Logins.comoAdmin;
import static com.egon.Logins.comoAluno;
import static com.egon.Logins.comoEmpresa;
import static com.egon.Logins.comoProfessor;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.matchesPattern;
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
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;

import com.egon.Dados;
import com.egon.dto.TransacaoRequest;
import com.egon.repository.CarteiraRepository;
import com.egon.repository.CursoRepository;
import com.egon.repository.InstituicaoRepository;
import com.egon.repository.ProfessorRepository;
import com.egon.service.TransacaoService;
import com.jayway.jsonpath.JsonPath;

/**
 * HU09 e HU10: o aluno resgata uma vantagem, recebe um cupom, e a empresa confere o código e
 * confirma o uso. Ana tem 100 Egos da Helena e 60 do Ricardo. Cada teste é desfeito no fim.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ResgateTest {

    static final String FORMATO = "EGON-[0-9A-HJKMNP-TV-Z]{4}-[0-9A-HJKMNP-TV-Z]{4}";

    @Autowired MockMvc mvc;
    @Autowired ProfessorRepository professores;
    @Autowired InstituicaoRepository instituicoes;
    @Autowired CursoRepository cursos;
    @Autowired CarteiraRepository carteiras;
    @Autowired TransacaoService transacoes;

    private UUID helena;
    private UUID ricardo;
    private UUID ana;
    private UUID bruno;
    private UUID livraria;
    private UUID papelaria;
    private UUID livro;

    @BeforeEach
    void cenario() throws Exception {
        helena = professores.findByCpf(Dados.HELENA).orElseThrow().getId();
        ricardo = professores.findByCpf(Dados.RICARDO).orElseThrow().getId();
        UUID horizonte = instituicoes.findByCnpj(Dados.HORIZONTE).orElseThrow().getId();
        int curso = cursos.findAllByOrderByNomeAsc().getFirst().getId();
        ana = Dados.cadastrarAluno(mvc, "Ana Souza", Dados.ANA, "ana", horizonte, curso);
        bruno = Dados.cadastrarAluno(mvc, "Bruno Lima", Dados.BRUNO, "bruno", horizonte, curso);
        transacoes.enviar(helena, new TransacaoRequest(ana, 100, "Monitoria"));
        transacoes.enviar(ricardo, new TransacaoRequest(ana, 60, "Participação"));
        livraria = Dados.cadastrarEmpresa(mvc, "Livraria Central", "45218073000192", "livraria");
        papelaria = Dados.cadastrarEmpresa(mvc, "Papelaria", "28039156000103", "papelaria");
        livro = Dados.cadastrarVantagem(mvc, livraria, "Livro da bibliografia", 120);
    }

    private ResultActions resgatar(UUID aluno, UUID vantagem) throws Exception {
        return mvc.perform(post("/alunos/" + aluno + "/resgates").with(comoAluno(aluno))
                .contentType(MediaType.APPLICATION_JSON).content("{\"vantagemId\": \"%s\"}".formatted(vantagem)));
    }

    private String codigo() throws Exception {
        return JsonPath.read(resgatar(ana, livro).andExpect(status().isCreated()).andReturn().getResponse().getContentAsString(),
                "$.codigo");
    }

    private int carteira(UUID aluno, UUID professor) {
        return carteiras.findByAlunoIdOrderByProfessorNomeAsc(aluno).stream()
                .filter(c -> c.getProfessor().getId().equals(professor)).mapToInt(c -> c.getSaldo()).sum();
    }

    private ResultActions confirmar(UUID empresa, String codigo, RequestPostProcessor quem) throws Exception {
        return mvc.perform(post("/empresas/" + empresa + "/cupons/" + codigo + "/confirmar").with(quem));
    }

    @Test
    @DisplayName("resgata: gera o cupom e debita primeiro a carteira com mais saldo")
    void resgata() throws Exception {
        resgatar(ana, livro)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.codigo").value(matchesPattern(FORMATO)))
                .andExpect(jsonPath("$.status").value("EMITIDO"))
                .andExpect(jsonPath("$.valor").value(120))
                .andExpect(jsonPath("$.vantagem.nome").value("Livro da bibliografia"))
                .andExpect(jsonPath("$.empresa.nome").value("Livraria Central"))
                .andExpect(jsonPath("$.aluno.nome").value("Ana Souza"))
                .andExpect(jsonPath("$.dataUso").doesNotExist());

        assertThat(carteira(ana, helena)).isZero();
        assertThat(carteira(ana, ricardo)).isEqualTo(40);
    }

    @Test
    @DisplayName("o resgate aparece no extrato como saída, com a empresa e o código do cupom")
    void extrato() throws Exception {
        String codigo = codigo();

        mvc.perform(get("/alunos/" + ana + "/extrato").with(comoAluno(ana)))
                .andExpect(jsonPath("$.saldo").value(40))
                .andExpect(jsonPath("$.movimentos[0].tipo").value("RESGATE"))
                .andExpect(jsonPath("$.movimentos[0].valor").value(120))
                .andExpect(jsonPath("$.movimentos[0].motivo").value("Livro da bibliografia"))
                .andExpect(jsonPath("$.movimentos[0].pessoa.nome").value("Livraria Central"))
                .andExpect(jsonPath("$.movimentos[0].cupom").value(codigo));
    }

    @Test
    @DisplayName("sem saldo total para o custo, nada é debitado")
    void saldoInsuficiente() throws Exception {
        UUID caro = Dados.cadastrarVantagem(mvc, livraria, "Curso de idiomas", 161);

        resgatar(ana, caro)
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.codigo").value("SALDO_INSUFICIENTE"));
        resgatar(bruno, livro).andExpect(status().isUnprocessableContent());
        assertThat(carteira(ana, helena)).isEqualTo(100);
        assertThat(carteira(ana, ricardo)).isEqualTo(60);
    }

    @Test
    @DisplayName("vantagem inexistente é 404; só o próprio aluno resgata")
    void acesso() throws Exception {
        resgatar(ana, UUID.randomUUID()).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value("VANTAGEM_NAO_ENCONTRADA"));
        for (RequestPostProcessor quem : new RequestPostProcessor[] {comoAluno(bruno), comoProfessor(helena), comoEmpresa(livraria), comoAdmin()}) {
            mvc.perform(post("/alunos/" + ana + "/resgates").with(quem)
                    .contentType(MediaType.APPLICATION_JSON).content("{\"vantagemId\": \"%s\"}".formatted(livro)))
                    .andExpect(status().isForbidden());
        }
        mvc.perform(get("/alunos/" + ana + "/cupons").with(comoAluno(bruno))).andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("a empresa acha o cupom pelo código digitado de qualquer jeito e confirma uma vez só")
    void empresaConfirma() throws Exception {
        String codigo = codigo();
        String digitado = " " + codigo.toLowerCase().replace("-", "") + " ";

        mvc.perform(get("/empresas/" + livraria + "/cupons/" + digitado).with(comoEmpresa(livraria)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigo").value(codigo))
                .andExpect(jsonPath("$.aluno.nome").value("Ana Souza"))
                .andExpect(jsonPath("$.status").value("EMITIDO"));

        confirmar(livraria, codigo, comoEmpresa(livraria))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("USADO"))
                .andExpect(jsonPath("$.dataUso").exists());
        confirmar(livraria, codigo, comoEmpresa(livraria))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("CUPOM_USADO"));

        mvc.perform(get("/alunos/" + ana + "/cupons").with(comoAluno(ana)))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].status").value("USADO"));
        mvc.perform(get("/empresas/" + livraria + "/cupons").with(comoEmpresa(livraria)))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].codigo").value(codigo));
    }

    @Test
    @DisplayName("cupom de outra empresa não existe para quem procura; código inventado também não")
    void outraEmpresa() throws Exception {
        String codigo = codigo();

        mvc.perform(get("/empresas/" + papelaria + "/cupons/" + codigo).with(comoEmpresa(papelaria)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value("CUPOM_NAO_ENCONTRADO"));
        confirmar(papelaria, codigo, comoEmpresa(papelaria)).andExpect(status().isNotFound());
        confirmar(livraria, codigo, comoEmpresa(papelaria)).andExpect(status().isForbidden());
        confirmar(livraria, "EGON-0000-0000", comoEmpresa(livraria)).andExpect(status().isNotFound());
        mvc.perform(get("/empresas/" + papelaria + "/cupons").with(comoEmpresa(papelaria))).andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("vantagem ou empresa com resgate não pode ser excluída")
    void exclusoes() throws Exception {
        codigo();

        mvc.perform(delete("/empresas/" + livraria + "/vantagens/" + livro).with(comoEmpresa(livraria)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("VANTAGEM_COM_RESGATES"));
        mvc.perform(delete("/empresas/" + livraria).with(comoAdmin()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("EMPRESA_COM_RESGATES"));
    }

}
