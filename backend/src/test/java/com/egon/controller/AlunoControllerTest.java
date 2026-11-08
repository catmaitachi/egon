package com.egon.controller;

import static com.egon.Dados.ANA;
import static com.egon.Dados.BRUNO;
import static com.egon.Dados.HORIZONTE;
import static com.egon.Logins.anonimo;
import static com.egon.Logins.comoAdmin;
import static com.egon.Logins.comoAluno;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import com.egon.Dados;
import com.egon.repository.AlunoRepository;
import com.egon.repository.CursoRepository;
import com.egon.repository.InstituicaoRepository;

/** O CRUD de aluno pela API. Cada teste roda numa transação desfeita no fim. */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AlunoControllerTest {

    @Autowired MockMvc mvc;
    @Autowired AlunoRepository alunos;
    @Autowired CursoRepository cursos;
    @Autowired InstituicaoRepository instituicoes;
    @Autowired PasswordEncoder senhas;

    private int curso;
    private UUID horizonte;

    @BeforeEach
    void listas() {
        curso = cursos.findAllByOrderByNomeAsc().getFirst().getId();
        horizonte = instituicoes.findByCnpj(HORIZONTE).orElseThrow().getId();
    }

    private String cadastro(String cpf, String email, String login) {
        return """
                {"nome": "Ana Souza", "email": "%s", "cpf": "%s", "rg": "MG-12.345.678",
                 "endereco": "Rua das Flores, 10 - Belo Horizonte", "idInstituicao": "%s",
                 "idCurso": %d, "login": "%s", "senha": "segredo123"}
                """.formatted(email, cpf, horizonte, curso, login);
    }

    private String edicao(String email, String login, String senha) {
        return """
                {"nome": "Ana Souza Lima", "email": "%s", "rg": "MG-12.345.678",
                 "endereco": "Av. Brasil, 500", "idInstituicao": "%s", "idCurso": %d,
                 "login": "%s", "senha": "%s"}
                """.formatted(email, horizonte, curso, login, senha);
    }

    private ResultActions enviar(String corpo) throws Exception {
        return mvc.perform(post("/alunos").with(anonimo()).contentType(MediaType.APPLICATION_JSON).content(corpo));
    }

    private UUID criar(String cpf, String email, String login) throws Exception {
        return Dados.id(enviar(cadastro(cpf, email, login)).andExpect(status().isCreated()).andReturn());
    }

    private ResultActions editar(UUID id, String corpo) throws Exception {
        return mvc.perform(put("/alunos/" + id).with(comoAluno(id))
                .contentType(MediaType.APPLICATION_JSON).content(corpo));
    }

    @Test
    @DisplayName("cadastra, busca e lista o aluno com id próprio, sem nunca devolver a senha")
    void cadastraBuscaELista() throws Exception {
        UUID id = Dados.id(enviar(cadastro(ANA, "ana@email.com", "ana"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.cpf").value(ANA))
                .andExpect(jsonPath("$.instituicao.id").value(horizonte.toString()))
                .andExpect(jsonPath("$.instituicao.nome").value("Universidade Horizonte"))
                .andExpect(jsonPath("$.curso.id").value(curso))
                .andExpect(jsonPath("$.saldo").value(0))
                .andExpect(jsonPath("$.senha").doesNotExist())
                .andReturn());

        mvc.perform(get("/alunos/" + id).with(comoAluno(id)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Ana Souza"))
                .andExpect(jsonPath("$.endereco").value("Rua das Flores, 10 - Belo Horizonte"));

        mvc.perform(get("/alunos").with(comoAdmin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(id.toString()))
                .andExpect(jsonPath("$[0].senha").doesNotExist());
    }

    @Test
    @DisplayName("o CPF não serve de id na rota")
    void rotaComCpfNaoAcha() throws Exception {
        criar(ANA, "ana@email.com", "ana");

        mvc.perform(get("/alunos/" + ANA).with(comoAdmin()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value("RECURSO_NAO_ENCONTRADO"));
    }

    @Test
    @DisplayName("grava a senha como hash BCrypt, não em texto")
    void gravaSenhaComoHash() throws Exception {
        UUID id = criar(ANA, "ana@email.com", "ana");

        String gravada = alunos.findById(id).orElseThrow().getSenha();
        assertThat(gravada).isNotEqualTo("segredo123");
        assertThat(senhas.matches("segredo123", gravada)).isTrue();
    }

    @Test
    @DisplayName("tira a máscara do CPF e guarda e-mail e login em minúsculas")
    void normalizaCampos() throws Exception {
        enviar(cadastro("529.982.247-25", " Ana@Email.COM ", "Ana.Souza"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.cpf").value(ANA))
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
                .andExpect(jsonPath("$.campos.idInstituicao").exists())
                .andExpect(jsonPath("$.campos.idCurso").exists())
                .andExpect(jsonPath("$.campos.login").exists())
                .andExpect(jsonPath("$.campos.senha").exists());
    }

    @Test
    @DisplayName("campo em branco recebe o aviso de obrigatório, não o de formato")
    void campoEmBrancoAvisaObrigatoriedade() throws Exception {
        enviar("""
                {"nome": "", "email": "", "cpf": "", "rg": "", "endereco": "", "idInstituicao": null,
                 "idCurso": null, "login": "", "senha": ""}
                """)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.email").value("Informe o e-mail."))
                .andExpect(jsonPath("$.campos.cpf").value("Informe o CPF."))
                .andExpect(jsonPath("$.campos.idInstituicao").value("Escolha a instituição."))
                .andExpect(jsonPath("$.campos.login").value("Escolha um login."))
                .andExpect(jsonPath("$.campos.senha").value("Crie uma senha."));
    }

    @Test
    @DisplayName("recusa e-mail sem domínio completo")
    void recusaEmailSemDominio() throws Exception {
        enviar(cadastro(ANA, "ana@teste", "ana"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.email").value("E-mail inválido."));
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
        criar(ANA, "ana@email.com", "ana");

        enviar(cadastro(ANA, "outra@email.com", "outra"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("CPF_EM_USO"))
                .andExpect(jsonPath("$.campos.cpf").exists());
    }

    @Test
    @DisplayName("recusa e-mail já cadastrado (HU02)")
    void recusaEmailDuplicado() throws Exception {
        criar(ANA, "ana@email.com", "ana");

        enviar(cadastro(BRUNO, "ANA@email.com", "outra"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("EMAIL_EM_USO"));
    }

    @Test
    @DisplayName("recusa login que já pertence a uma empresa: o login é único no sistema todo")
    void recusaLoginDeEmpresa() throws Exception {
        Dados.cadastrarEmpresa(mvc, "Livraria Central", "11222333000181", "livraria");

        enviar(cadastro(ANA, "ana@email.com", "livraria"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("LOGIN_EM_USO"));
    }

    @Test
    @DisplayName("recusa login que já pertence a um professor pré-cadastrado")
    void recusaLoginDeProfessor() throws Exception {
        enviar(cadastro(ANA, "ana@email.com", "helena.duarte"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("LOGIN_EM_USO"));
    }

    @Test
    @DisplayName("recusa instituição que não existe")
    void recusaInstituicaoForaDaLista() throws Exception {
        enviar(cadastro(ANA, "ana@email.com", "ana").replace(horizonte.toString(), UUID.randomUUID().toString()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("INSTITUICAO_NAO_ENCONTRADA"))
                .andExpect(jsonPath("$.campos.idInstituicao").exists());
    }

    @Test
    @DisplayName("edita os dados e mantém a senha quando ela vem em branco")
    void editaMantendoSenha() throws Exception {
        UUID id = criar(ANA, "ana@email.com", "ana");
        String antes = alunos.findById(id).orElseThrow().getSenha();

        editar(id, edicao("ana.lima@email.com", "ana.lima", ""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Ana Souza Lima"))
                .andExpect(jsonPath("$.email").value("ana.lima@email.com"))
                .andExpect(jsonPath("$.login").value("ana.lima"))
                .andExpect(jsonPath("$.endereco").value("Av. Brasil, 500"))
                .andExpect(jsonPath("$.cpf").value(ANA))
                .andExpect(jsonPath("$.saldo").value(0));

        assertThat(alunos.findById(id).orElseThrow().getSenha()).isEqualTo(antes);
    }

    @Test
    @DisplayName("troca a senha quando uma nova é informada")
    void trocaSenha() throws Exception {
        UUID id = criar(ANA, "ana@email.com", "ana");

        editar(id, edicao("ana@email.com", "ana", "novaSenha456")).andExpect(status().isOk());

        assertThat(senhas.matches("novaSenha456", alunos.findById(id).orElseThrow().getSenha())).isTrue();
    }

    @Test
    @DisplayName("não deixa a edição usar o e-mail de outro aluno")
    void edicaoRecusaEmailDeOutro() throws Exception {
        UUID id = criar(ANA, "ana@email.com", "ana");
        criar(BRUNO, "bruno@email.com", "bruno");

        editar(id, edicao("bruno@email.com", "ana", ""))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("EMAIL_EM_USO"));
    }

    @Test
    @DisplayName("exclui o aluno, que deixa de ser encontrado")
    void exclui() throws Exception {
        UUID id = criar(ANA, "ana@email.com", "ana");

        mvc.perform(delete("/alunos/" + id).with(comoAdmin())).andExpect(status().isNoContent());

        mvc.perform(get("/alunos/" + id).with(comoAdmin()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value("ALUNO_NAO_ENCONTRADO"));
    }

    @Test
    @DisplayName("responde 404 ao editar ou excluir aluno inexistente")
    void inexistente() throws Exception {
        UUID ninguem = UUID.randomUUID();
        mvc.perform(put("/alunos/" + ninguem).with(comoAdmin())
                        .contentType(MediaType.APPLICATION_JSON).content(edicao("ana@email.com", "ana", "")))
                .andExpect(status().isNotFound());
        mvc.perform(delete("/alunos/" + ninguem).with(comoAdmin())).andExpect(status().isNotFound());
    }

}
