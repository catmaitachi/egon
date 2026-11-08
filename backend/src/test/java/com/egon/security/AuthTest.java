package com.egon.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import com.egon.Dados;
import com.egon.dto.AlunoCadastroRequest;
import com.egon.dto.EmpresaCadastroRequest;
import com.egon.repository.CursoRepository;
import com.egon.repository.InstituicaoRepository;
import com.egon.service.AlunoService;
import com.egon.service.EmpresaService;

import jakarta.servlet.http.Cookie;

/**
 * Login de verdade (HU01): sessão no cookie e token de CSRF lido do cookie, como o front faz.
 *
 * <p>Contexto próprio (a propriedade só muda a chave do cache de contextos): o {@code csrf()} do
 * spring-security-test, usado nos outros testes, troca de vez o repositório de tokens do filtro
 * compartilhado por um de sessão, e o cookie deixaria de existir. Por isso aqui ninguém usa
 * {@code Logins}, e os cadastros vão direto pelos serviços.
 */
@SpringBootTest(properties = "egon.teste=autenticacao")
@AutoConfigureMockMvc
@Transactional
class AuthTest {

    @Autowired MockMvc mvc;
    @Autowired CursoRepository cursos;
    @Autowired InstituicaoRepository instituicoes;
    @Autowired AlunoService alunos;
    @Autowired EmpresaService empresas;

    /** O navegador: guarda a sessão e o cookie de CSRF entre as requisições. */
    private final class Navegador {
        MockHttpSession sessao = new MockHttpSession();
        Cookie token;

        Navegador() throws Exception {
            atualizar(mvc.perform(get("/auth/eu").session(sessao)).andReturn());
        }

        /** Como o navegador: vale o último Set-Cookie (o login apaga o token antigo e manda o novo). */
        void atualizar(MvcResult resultado) {
            for (Cookie cookie : resultado.getResponse().getCookies()) {
                if (cookie.getName().equals("XSRF-TOKEN")) {
                    token = cookie;
                }
            }
            if (resultado.getRequest().getSession(false) instanceof MockHttpSession atual) {
                sessao = atual;
            }
        }

        ResultActions entrar(String login, String senha) throws Exception {
            ResultActions resposta = mvc.perform(post("/auth/login").session(sessao).cookie(token)
                    .header("X-XSRF-TOKEN", token.getValue())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                            {"login": "%s", "senha": "%s"}
                            """.formatted(login, senha)));
            atualizar(resposta.andReturn());
            return resposta;
        }

        ResultActions eu() throws Exception {
            ResultActions resposta = mvc.perform(get("/auth/eu").session(sessao).cookie(token));
            atualizar(resposta.andReturn());
            return resposta;
        }

        ResultActions sair() throws Exception {
            return mvc.perform(post("/auth/logout").session(sessao).cookie(token).header("X-XSRF-TOKEN", token.getValue()));
        }
    }

    private UUID ana() {
        return alunos.cadastrar(new AlunoCadastroRequest("Ana Souza", "ana@email.com", Dados.ANA, "123", "Rua A, 1",
                instituicoes.findByCnpj(Dados.HORIZONTE).orElseThrow().getId(),
                cursos.findAllByOrderByNomeAsc().getFirst().getId(), "ana", "segredo123")).id();
    }

    @Test
    @DisplayName("o login de cada perfil devolve perfil, id e nome, e a sessão fica de pé")
    void loginDeCadaPerfil() throws Exception {
        UUID ana = ana();
        UUID livraria = empresas.cadastrar(new EmpresaCadastroRequest("Livraria Central", "45218073000192",
                "contato@livraria.com", "livraria", "segredo123")).id();

        Navegador aluno = new Navegador();
        aluno.entrar("ana", "segredo123")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.perfil").value("ALUNO"))
                .andExpect(jsonPath("$.id").value(ana.toString()))
                .andExpect(jsonPath("$.nome").value("Ana Souza"));
        aluno.eu().andExpect(status().isOk()).andExpect(jsonPath("$.id").value(ana.toString()));

        new Navegador().entrar("helena.duarte", "professor123")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.perfil").value("PROFESSOR"))
                .andExpect(jsonPath("$.nome").value("Helena Duarte"));
        new Navegador().entrar("livraria", "segredo123")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.perfil").value("EMPRESA"))
                .andExpect(jsonPath("$.id").value(livraria.toString()));
        new Navegador().entrar("admin", "admin12345")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.perfil").value("ADMIN"));
    }

    @Test
    @DisplayName("senha errada e login inexistente têm a mesma resposta (HU01)")
    void credenciaisInvalidas() throws Exception {
        new Navegador().entrar("helena.duarte", "errada123")
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.codigo").value("CREDENCIAIS_INVALIDAS"))
                .andExpect(jsonPath("$.mensagem").value("Login ou senha inválidos."));
        new Navegador().entrar("ninguem", "errada123")
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.codigo").value("CREDENCIAIS_INVALIDAS"))
                .andExpect(jsonPath("$.mensagem").value("Login ou senha inválidos."));
    }

    @Test
    @DisplayName("o login ignora maiúsculas e espaços em volta, como o cadastro guarda")
    void loginIgnoraCaixaEEspacos() throws Exception {
        new Navegador().entrar(" Helena.Duarte ", "professor123").andExpect(status().isOk());
    }

    @Test
    @DisplayName("sem sessão, /auth/eu responde 401 e já entrega o cookie de CSRF")
    void euSemSessao() throws Exception {
        mvc.perform(get("/auth/eu"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.codigo").value("NAO_AUTENTICADO"))
                .andExpect(cookie().exists("XSRF-TOKEN"));
    }

    @Test
    @DisplayName("depois de sair, a sessão não vale mais")
    void sair() throws Exception {
        Navegador navegador = new Navegador();
        navegador.entrar("helena.duarte", "professor123").andExpect(status().isOk());

        navegador.sair().andExpect(status().isNoContent());

        navegador.eu().andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("escrita sem o cabeçalho de CSRF é recusada com 403")
    void escritaSemCsrfDa403() throws Exception {
        mvc.perform(post("/alunos").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.codigo").value("CSRF_INVALIDO"));
    }

    @Test
    @DisplayName("o token de CSRF muda no login: o cabeçalho antigo deixa de valer e o novo, lido do cookie, funciona")
    void postDepoisDoLoginUsaTokenNovo() throws Exception {
        Navegador navegador = new Navegador();
        String antigo = navegador.token.getValue();
        navegador.entrar("admin", "admin12345").andExpect(status().isOk());

        // O navegador já guarda o cookie novo; quem reaproveita o valor antigo no cabeçalho é recusado.
        mvc.perform(post("/auth/logout").session(navegador.sessao).cookie(navegador.token).header("X-XSRF-TOKEN", antigo))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.codigo").value("CSRF_INVALIDO"));
        navegador.sair().andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("conta excluída enquanto logada perde a sessão na requisição seguinte")
    void contaExcluidaPerdeASessao() throws Exception {
        UUID ana = ana();
        Navegador navegador = new Navegador();
        navegador.entrar("ana", "segredo123").andExpect(status().isOk());

        alunos.excluir(ana);

        navegador.eu().andExpect(status().isUnauthorized());
    }

}
