package com.egon.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.egon.Dados;
import com.egon.model.CotaSemestral;
import com.egon.repository.CotaSemestralRepository;
import com.egon.repository.ProfessorRepository;

/** A cota de 1000 por semestre (HU05). Cada teste roda numa transação desfeita no fim. */
@SpringBootTest
@Transactional
class CotaSemestralServiceTest {

    @Autowired ProfessorRepository professores;
    @Autowired CotaSemestralRepository cotas;

    /** Helena Duarte, pré-cadastrada. */
    private UUID helena;

    @BeforeEach
    void helena() {
        helena = professores.findByCpf(Dados.HELENA).orElseThrow().getId();
    }

    /** O serviço com o relógio parado no instante dado, em UTC: o fuso de São Paulo é por conta dele. */
    private CotaSemestralService em(String instante) {
        return new CotaSemestralService(professores, cotas, Clock.fixed(Instant.parse(instante), ZoneOffset.UTC));
    }

    private int saldo(UUID id) {
        return professores.findById(id).orElseThrow().getSaldo();
    }

    @Test
    @DisplayName("a subida da aplicação credita a cota do semestre atual uma vez a cada professor")
    void creditaNaSubida() {
        assertThat(professores.findAll()).hasSize(4).allSatisfy(professor -> {
            assertThat(professor.getSaldo()).isEqualTo(1000);
            assertThat(cotas.findByProfessorIdOrderByDataDesc(professor.getId())).hasSize(1);
        });
    }

    @Test
    @DisplayName("credita 1000 no semestre novo somando ao saldo que sobrou, sem zerar")
    void acumula() {
        professores.findById(helena).orElseThrow().setSaldo(350);

        em("2099-03-10T12:00:00Z").creditar();

        assertThat(saldo(helena)).isEqualTo(1350);
        CotaSemestral cota = cotas.findByProfessorIdOrderByDataDesc(helena).getFirst();
        assertThat(cota.getSemestre()).isEqualTo("2099.1");
        assertThat(cota.getValor()).isEqualTo(1000);
        assertThat(cota.getData()).isEqualTo(Instant.parse("2099-03-10T12:00:00Z"));
    }

    @Test
    @DisplayName("rodar duas vezes no mesmo semestre credita uma vez só")
    void idempotente() {
        CotaSemestralService servico = em("2099-03-10T12:00:00Z");

        servico.creditar();
        servico.creditar();

        assertThat(saldo(helena)).isEqualTo(2000);
        assertThat(cotas.findByProfessorIdOrderByDataDesc(helena)).hasSize(2);
    }

    @ParameterizedTest(name = "{0} cai em {1}")
    @CsvSource({
            "2099-07-01T02:59:59Z, 2099.1", // 30/06 23:59:59 em São Paulo
            "2099-07-01T03:00:00Z, 2099.2", // 01/07 00:00:00 em São Paulo
            "2100-01-01T02:59:59Z, 2099.2", // 31/12 23:59:59 em São Paulo
            "2100-01-01T03:00:00Z, 2100.1"}) // 01/01 00:00:00 em São Paulo
    @DisplayName("usa o semestre do fuso de São Paulo nas viradas de 30/06 para 01/07 e de 31/12 para 01/01")
    void viradas(String instante, String semestre) {
        em(instante).creditar();

        assertThat(cotas.findByProfessorIdOrderByDataDesc(helena).getFirst().getSemestre()).isEqualTo(semestre);
    }

}
