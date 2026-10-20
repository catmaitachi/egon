package com.egon.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.egon.model.Professor;
import com.egon.repository.CotaSemestralRepository;
import com.egon.repository.ProfessorRepository;

/** HU05: 1.000 moedas por semestre, somadas ao que sobrou. */
@SpringBootTest
@Transactional
class CotaSemestralTest {

    @Autowired ProfessorService servico;
    @Autowired ProfessorRepository professores;
    @Autowired CotaSemestralRepository cotas;
    @Autowired Clock relogio;

    @Test
    @DisplayName("na subida, cada professor pré-cadastrado recebe a cota do semestre atual")
    void creditaNaSubida() {
        String atual = CotaSemestralScheduler.semestre(LocalDate.now(relogio));
        assertThat(professores.findAll()).isNotEmpty().allSatisfy(professor -> {
            assertThat(professor.getSaldo()).isEqualTo(1000);
            assertThat(cotas.existsByProfessorCpfAndSemestre(professor.getCpf(), atual)).isTrue();
        });
    }

    @Test
    @DisplayName("rodar de novo no mesmo semestre não credita duas vezes")
    void naoDuplica() {
        String atual = CotaSemestralScheduler.semestre(LocalDate.now(relogio));
        assertThat(servico.creditarCotaSemestral(atual)).isZero();
        assertThat(professores.findAll()).allSatisfy(p -> assertThat(p.getSaldo()).isEqualTo(1000));
    }

    @Test
    @DisplayName("o saldo que sobrou acumula: 350 + 1.000 = 1.350")
    void acumula() {
        Professor helena = professores.findByLogin("helena.duarte").orElseThrow();
        helena.setSaldo(350);

        assertThat(servico.creditarCotaSemestral("2099.1")).isEqualTo(professores.count());

        assertThat(professores.findByLogin("helena.duarte").orElseThrow().getSaldo()).isEqualTo(1350);
        assertThat(cotas.findByProfessorCpf(helena.getCpf())).extracting("semestre").contains("2099.1");
    }

    @Test
    @DisplayName("janeiro a junho é o 1º semestre; julho a dezembro, o 2º")
    void semestre() {
        assertThat(CotaSemestralScheduler.semestre(LocalDate.of(2026, 6, 30))).isEqualTo("2026.1");
        assertThat(CotaSemestralScheduler.semestre(LocalDate.of(2026, 7, 1))).isEqualTo("2026.2");
    }

}
