package com.egon.config;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.egon.Dados;
import com.egon.dto.AlunoCadastroRequest;
import com.egon.repository.CotaSemestralRepository;
import com.egon.repository.CursoRepository;
import com.egon.repository.InstituicaoRepository;
import com.egon.repository.ProfessorRepository;
import com.egon.service.AlunoService;

/** A carga sobe também sobre bancos antigos, que já têm alunos e empresas. Cada teste é desfeito no fim. */
@SpringBootTest
@Transactional
class DadosIniciaisTest {

    @Autowired DadosIniciais carga;
    @Autowired ProfessorRepository professores;
    @Autowired CotaSemestralRepository cotas;
    @Autowired CursoRepository cursos;
    @Autowired AlunoService alunoService;
    @Autowired InstituicaoRepository instituicoes;

    @Test
    @DisplayName("não pré-cadastra professor com um login que já é de um aluno")
    void loginDeOutroPerfil() {
        cotas.deleteAll();
        professores.deleteAll();
        alunoService.cadastrar(new AlunoCadastroRequest("Ana Souza", "ana@email.com", "52998224725", "123",
                "Rua A, 1", instituicoes.findByCnpj(Dados.HORIZONTE).orElseThrow().getId(), cursos.findAllByOrderByNomeAsc().getFirst().getId(),
                "helena.duarte", "segredo123"));

        assertThatThrownBy(carga::afterSingletonsInstantiated).hasMessageContaining("helena.duarte");
    }

}
