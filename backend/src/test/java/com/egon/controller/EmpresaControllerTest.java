package com.egon.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import com.egon.repository.CursoRepository;
import com.egon.repository.EmpresaRepository;

/** O CRUD de empresa parceira pela API. Cada teste roda numa transação desfeita no fim. */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class EmpresaControllerTest {

    private static final String CNPJ = "45218073000192";

    @Autowired MockMvc mvc;
    @Autowired EmpresaRepository empresas;
    @Autowired CursoRepository cursos;
    @Autowired PasswordEncoder senhas;

    private static String cadastro(String cnpj, String email, String login) {
        return """
                {"nome": "Livraria Central", "cnpj": "%s", "email": "%s", "login": "%s", "senha": "segredo123"}
                """.formatted(cnpj, email, login);
    }

    private ResultActions enviar(String corpo) throws Exception {
        return mvc.perform(post("/empresas").contentType(MediaType.APPLICATION_JSON).content(corpo));
    }

    @Test
    @DisplayName("cadastra, busca e lista a empresa sem nunca devolver a senha")
    void cadastraBuscaELista() throws Exception {
        enviar(cadastro(CNPJ, "contato@livraria.com", "livraria"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.cnpj").value(CNPJ))
                .andExpect(jsonPath("$.senha").doesNotExist());

        mvc.perform(get("/empresas/" + CNPJ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Livraria Central"));

        mvc.perform(get("/empresas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].cnpj").value(CNPJ))
                .andExpect(jsonPath("$[0].senha").doesNotExist());

        String gravada = empresas.findById(CNPJ).orElseThrow().getSenha();
        assertThat(senhas.matches("segredo123", gravada)).isTrue();
    }

    @Test
    @DisplayName("aceita CNPJ alfanumérico com máscara e guarda só os caracteres")
    void aceitaCnpjAlfanumerico() throws Exception {
        enviar(cadastro("12.abc.345/01de-35", "contato@livraria.com", "livraria"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.cnpj").value("12ABC34501DE35"));

        mvc.perform(get("/empresas/12ABC34501DE35")).andExpect(status().isOk());
    }

    @Test
    @DisplayName("recusa CNPJ com dígito verificador errado (HU03)")
    void recusaCnpjInvalido() throws Exception {
        enviar(cadastro("45218073000191", "contato@livraria.com", "livraria"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.cnpj").exists());
    }

    @Test
    @DisplayName("campo em branco recebe o aviso de obrigatório, não o de formato")
    void campoEmBrancoAvisaObrigatoriedade() throws Exception {
        enviar("""
                {"nome": "", "cnpj": "", "email": "", "login": "", "senha": ""}
                """)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.cnpj").value("Informe o CNPJ."))
                .andExpect(jsonPath("$.campos.email").value("Informe o e-mail de contato."))
                .andExpect(jsonPath("$.campos.login").value("Escolha um login."));
    }

    @Test
    @DisplayName("recusa CNPJ já cadastrado (HU03)")
    void recusaCnpjDuplicado() throws Exception {
        enviar(cadastro(CNPJ, "contato@livraria.com", "livraria")).andExpect(status().isCreated());

        enviar(cadastro("45.218.073/0001-92", "outro@livraria.com", "outra"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("CNPJ_EM_USO"))
                .andExpect(jsonPath("$.campos.cnpj").exists());
    }

    @Test
    @DisplayName("recusa login que já pertence a um aluno: o login é único no sistema todo")
    void recusaLoginDeAluno() throws Exception {
        int curso = cursos.findAllByOrderByNomeAsc().getFirst().getId();
        mvc.perform(post("/alunos").contentType(MediaType.APPLICATION_JSON).content("""
                {"nome": "Ana Souza", "email": "ana@email.com", "cpf": "52998224725", "rg": "123",
                 "endereco": "Rua A, 1", "cnpjInstituicao": "11222333000181", "idCurso": %d,
                 "login": "ana", "senha": "segredo123"}
                """.formatted(curso))).andExpect(status().isCreated());

        enviar(cadastro(CNPJ, "contato@livraria.com", "ana"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("LOGIN_EM_USO"));
    }

    @Test
    @DisplayName("edita nome, e-mail e login e mantém a senha quando ela vem em branco")
    void editaMantendoSenha() throws Exception {
        enviar(cadastro(CNPJ, "contato@livraria.com", "livraria")).andExpect(status().isCreated());
        String antes = empresas.findById(CNPJ).orElseThrow().getSenha();

        mvc.perform(put("/empresas/" + CNPJ).contentType(MediaType.APPLICATION_JSON).content("""
                {"nome": "Livraria Central Ltda", "email": "vendas@livraria.com", "login": "livraria.central", "senha": ""}
                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Livraria Central Ltda"))
                .andExpect(jsonPath("$.email").value("vendas@livraria.com"))
                .andExpect(jsonPath("$.login").value("livraria.central"));

        assertThat(empresas.findById(CNPJ).orElseThrow().getSenha()).isEqualTo(antes);
    }

    @Test
    @DisplayName("exclui a empresa, que deixa de ser encontrada")
    void exclui() throws Exception {
        enviar(cadastro(CNPJ, "contato@livraria.com", "livraria")).andExpect(status().isCreated());

        mvc.perform(delete("/empresas/" + CNPJ)).andExpect(status().isNoContent());

        mvc.perform(get("/empresas/" + CNPJ))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value("EMPRESA_NAO_ENCONTRADA"));
    }

}
