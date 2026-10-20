package com.egon.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
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

import com.egon.repository.AlunoRepository;
import com.egon.repository.CursoRepository;

/** O CRUD de aluno pela API. Cada teste roda numa transação desfeita no fim. */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AlunoControllerTest {

    private static final String INSTITUICAO = "11222333000181";
    private static final String CPF = "52998224725";
    private static final String OUTRO_CPF = "11144477735";

    @Autowired MockMvc mvc;
    @Autowired AlunoRepository alunos;
    @Autowired CursoRepository cursos;
    @Autowired PasswordEncoder senhas;

    private int curso;

    @BeforeEach
    void cursoDaLista() {
        curso = cursos.findAllByOrderByNomeAsc().getFirst().getId();
    }

    private String cadastro(String cpf, String email, String login) {
        return """
                {"nome": "Ana Souza", "email": "%s", "cpf": "%s", "rg": "MG-12.345.678",
                 "endereco": "Rua das Flores, 10 - Belo Horizonte", "cnpjInstituicao": "%s",
                 "idCurso": %d, "login": "%s", "senha": "segredo123"}
                """.formatted(email, cpf, INSTITUICAO, curso, login);
    }

    private String edicao(String email, String login, String senha) {
        return """
                {"nome": "Ana Souza Lima", "email": "%s", "rg": "MG-12.345.678",
                 "endereco": "Av. Brasil, 500", "cnpjInstituicao": "%s", "idCurso": %d,
                 "login": "%s", "senha": "%s"}
                """.formatted(email, INSTITUICAO, curso, login, senha);
    }

    private ResultActions enviar(String corpo) throws Exception {
        return mvc.perform(post("/alunos").contentType(MediaType.APPLICATION_JSON).content(corpo));
    }

    private ResultActions editar(String cpf, String corpo) throws Exception {
        return mvc.perform(put("/alunos/" + cpf).contentType(MediaType.APPLICATION_JSON).content(corpo));
    }

    @Test
    @DisplayName("cadastra, busca e lista o aluno sem nunca devolver a senha")
    void cadastraBuscaELista() throws Exception {
        enviar(cadastro(CPF, "ana@email.com", "ana"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.cpf").value(CPF))
                .andExpect(jsonPath("$.instituicao.nome").value("Universidade Horizonte"))
                .andExpect(jsonPath("$.curso.id").value(curso))
                .andExpect(jsonPath("$.senha").doesNotExist());

        mvc.perform(get("/alunos/" + CPF))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Ana Souza"))
                .andExpect(jsonPath("$.endereco").value("Rua das Flores, 10 - Belo Horizonte"));

        mvc.perform(get("/alunos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].cpf").value(CPF))
                .andExpect(jsonPath("$[0].senha").doesNotExist());
    }

    @Test
    @DisplayName("grava a senha como hash BCrypt, não em texto")
    void gravaSenhaComoHash() throws Exception {
        enviar(cadastro(CPF, "ana@email.com", "ana")).andExpect(status().isCreated());

        String gravada = alunos.findById(CPF).orElseThrow().getSenha();
        assertThat(gravada).isNotEqualTo("segredo123");
        assertThat(senhas.matches("segredo123", gravada)).isTrue();
    }

    @Test
    @DisplayName("tira a máscara do CPF e guarda e-mail e login em minúsculas")
    void normalizaCampos() throws Exception {
        enviar(cadastro("529.982.247-25", " Ana@Email.COM ", "Ana.Souza"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.cpf").value(CPF))
                .andExpect(jsonPath("$.email").value("ana@email.com"))
                .andExpect(jsonPath("$.login").value("ana.souza"));
    }

    @Test
    @DisplayName("aponta cada campo obrigatório que veio vazio")
    void exigeCamposObrigatorios() throws Exception {
        enviar("{}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("DADOS_INVALIDOS"))
                .andExpect(jsonPath("$.campos.nome").exists())
                .andExpect(jsonPath("$.campos.email").exists())
                .andExpect(jsonPath("$.campos.cpf").exists())
                .andExpect(jsonPath("$.campos.rg").exists())
                .andExpect(jsonPath("$.campos.endereco").exists())
                .andExpect(jsonPath("$.campos.cnpjInstituicao").exists())
                .andExpect(jsonPath("$.campos.idCurso").exists())
                .andExpect(jsonPath("$.campos.login").exists())
                .andExpect(jsonPath("$.campos.senha").exists());
    }

    @Test
    @DisplayName("recusa CPF com dígito verificador errado")
    void recusaCpfInvalido() throws Exception {
        enviar(cadastro("52998224724", "ana@email.com", "ana"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.cpf").value("CPF inválido. Confira os 11 dígitos."));
    }

    @Test
    @DisplayName("recusa CPF já cadastrado (HU02)")
    void recusaCpfDuplicado() throws Exception {
        enviar(cadastro(CPF, "ana@email.com", "ana")).andExpect(status().isCreated());

        enviar(cadastro(CPF, "outra@email.com", "outra"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("CPF_EM_USO"))
                .andExpect(jsonPath("$.campos.cpf").exists());
    }

    @Test
    @DisplayName("recusa e-mail já cadastrado (HU02)")
    void recusaEmailDuplicado() throws Exception {
        enviar(cadastro(CPF, "ana@email.com", "ana")).andExpect(status().isCreated());

        enviar(cadastro(OUTRO_CPF, "ANA@email.com", "outra"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("EMAIL_EM_USO"));
    }

    @Test
    @DisplayName("recusa login que já pertence a uma empresa: o login é único no sistema todo")
    void recusaLoginDeEmpresa() throws Exception {
        mvc.perform(post("/empresas").contentType(MediaType.APPLICATION_JSON).content("""
                {"nome": "Livraria Central", "cnpj": "11222333000181", "email": "contato@livraria.com",
                 "login": "livraria", "senha": "segredo123"}
                """)).andExpect(status().isCreated());

        enviar(cadastro(CPF, "ana@email.com", "livraria"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("LOGIN_EM_USO"));
    }

    @Test
    @DisplayName("recusa instituição que não está na lista de conveniadas")
    void recusaInstituicaoForaDaLista() throws Exception {
        enviar(cadastro(CPF, "ana@email.com", "ana").replace(INSTITUICAO, "73401522000179"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("INSTITUICAO_NAO_ENCONTRADA"))
                .andExpect(jsonPath("$.campos.cnpjInstituicao").exists());
    }

    @Test
    @DisplayName("edita os dados e mantém a senha quando ela vem em branco")
    void editaMantendoSenha() throws Exception {
        enviar(cadastro(CPF, "ana@email.com", "ana")).andExpect(status().isCreated());
        String antes = alunos.findById(CPF).orElseThrow().getSenha();

        editar(CPF, edicao("ana.lima@email.com", "ana.lima", ""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Ana Souza Lima"))
                .andExpect(jsonPath("$.email").value("ana.lima@email.com"))
                .andExpect(jsonPath("$.login").value("ana.lima"))
                .andExpect(jsonPath("$.endereco").value("Av. Brasil, 500"));

        assertThat(alunos.findById(CPF).orElseThrow().getSenha()).isEqualTo(antes);
    }

    @Test
    @DisplayName("troca a senha quando uma nova é informada")
    void trocaSenha() throws Exception {
        enviar(cadastro(CPF, "ana@email.com", "ana")).andExpect(status().isCreated());

        editar(CPF, edicao("ana@email.com", "ana", "novaSenha456")).andExpect(status().isOk());

        assertThat(senhas.matches("novaSenha456", alunos.findById(CPF).orElseThrow().getSenha())).isTrue();
    }

    @Test
    @DisplayName("não deixa a edição usar o e-mail de outro aluno")
    void edicaoRecusaEmailDeOutro() throws Exception {
        enviar(cadastro(CPF, "ana@email.com", "ana")).andExpect(status().isCreated());
        enviar(cadastro(OUTRO_CPF, "bruno@email.com", "bruno")).andExpect(status().isCreated());

        editar(CPF, edicao("bruno@email.com", "ana", ""))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("EMAIL_EM_USO"));
    }

    @Test
    @DisplayName("exclui o aluno, que deixa de ser encontrado")
    void exclui() throws Exception {
        enviar(cadastro(CPF, "ana@email.com", "ana")).andExpect(status().isCreated());

        mvc.perform(delete("/alunos/" + CPF)).andExpect(status().isNoContent());

        mvc.perform(get("/alunos/" + CPF))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value("ALUNO_NAO_ENCONTRADO"));
    }

    @Test
    @DisplayName("responde 404 ao editar ou excluir aluno inexistente")
    void inexistente() throws Exception {
        editar(CPF, edicao("ana@email.com", "ana", "")).andExpect(status().isNotFound());
        mvc.perform(delete("/alunos/" + CPF)).andExpect(status().isNotFound());
    }

}
