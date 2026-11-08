package com.egon.controller;

import static com.egon.Logins.comoAdmin;
import static com.egon.Logins.comoProfessor;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.egon.model.Professor;
import com.egon.repository.ProfessorRepository;
import com.egon.validation.Documentos;

/** Os professores pré-cadastrados (HU04), só leitura pela API. */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ProfessorControllerTest {

    /** Helena Duarte, Universidade Horizonte. */
    private static final String HELENA = "48291573646";

    @Autowired MockMvc mvc;
    @Autowired ProfessorRepository professores;
    @Autowired PasswordEncoder senhas;

    @Test
    @DisplayName("lista os 4 professores pré-cadastrados por nome, com a cota do semestre e sem a senha")
    void lista() throws Exception {
        mvc.perform(get("/professores").with(comoAdmin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(4)))
                .andExpect(jsonPath("$[*].nome").value(contains(
                        "Beatriz Campos", "Helena Duarte", "Marcelo Tavares", "Ricardo Moraes")))
                .andExpect(jsonPath("$[*].saldo").value(everyItem(is(1000))))
                .andExpect(jsonPath("$[*].instituicao.cnpj").value(contains(
                        "45218073000192", "11222333000181", "28039156000103", "11222333000181")))
                .andExpect(jsonPath("$[*].id").exists())
                .andExpect(jsonPath("$[0].senha").doesNotExist());
    }

    @Test
    @DisplayName("busca o professor pelo id")
    void busca() throws Exception {
        UUID id = professores.findByCpf(HELENA).orElseThrow().getId();
        mvc.perform(get("/professores/" + id).with(comoProfessor(id)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cpf").value(HELENA))
                .andExpect(jsonPath("$.nome").value("Helena Duarte"))
                .andExpect(jsonPath("$.departamento").exists())
                .andExpect(jsonPath("$.instituicao.nome").value("Universidade Horizonte"))
                .andExpect(jsonPath("$.login").value("helena.duarte"))
                .andExpect(jsonPath("$.saldo").value(1000))
                .andExpect(jsonPath("$.senha").doesNotExist());
    }

    @Test
    @DisplayName("responde 404 com código para professor inexistente")
    void inexistente() throws Exception {
        mvc.perform(get("/professores/" + UUID.randomUUID()).with(comoAdmin()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value("PROFESSOR_NAO_ENCONTRADO"));
    }

    @Test
    @DisplayName("pré-cadastra CPFs válidos, login nome.sobrenome e a senha inicial como hash BCrypt")
    void dadosDoPreCadastro() {
        List<Professor> todos = professores.findAll();

        assertThat(todos).allSatisfy(professor -> {
            assertThat(Documentos.cpfValido(professor.getCpf())).isTrue();
            assertThat(professor.getLogin()).matches("[a-z]+\\.[a-z]+");
            assertThat(professor.getSenha()).isNotEqualTo("professor123");
            assertThat(senhas.matches("professor123", professor.getSenha())).isTrue();
        });
    }

}
