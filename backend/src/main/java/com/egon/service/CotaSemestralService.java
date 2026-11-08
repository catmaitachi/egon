package com.egon.service;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.UUID;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.egon.model.CotaSemestral;
import com.egon.model.Professor;
import com.egon.repository.CotaSemestralRepository;
import com.egon.repository.ProfessorRepository;

import lombok.RequiredArgsConstructor;

/** HU05: todo semestre cada professor recebe 1000 moedas, somadas ao que sobrou. */
@Service
@RequiredArgsConstructor
public class CotaSemestralService {

    static final int VALOR = 1000;

    /** O semestre é o do calendário da instituição, não o do relógio do servidor. */
    private static final ZoneId FUSO = ZoneId.of("America/Sao_Paulo");

    private final ProfessorRepository professores;
    private final CotaSemestralRepository cotas;
    private final Clock relogio;

    /**
     * Idempotente: só credita a quem ainda não tem cota no semestre. Roda na subida (pelo
     * {@code DadosIniciais}) e na virada do semestre; se o servidor estava fora na virada, a
     * próxima subida cobre.
     */
    @Scheduled(cron = "0 5 0 1 1,7 *", zone = "America/Sao_Paulo")
    @Transactional
    public void creditar() {
        Instant agora = relogio.instant();
        String semestre = semestre(agora);
        for (UUID id : professores.findIdsSemCota(semestre)) {
            // A trava evita perder um envio que esteja debitando o mesmo saldo agora.
            Professor professor = professores.findBloqueadoById(id).orElseThrow();
            professor.setSaldo(professor.getSaldo() + VALOR);
            cotas.save(new CotaSemestral(professor, semestre, VALOR, agora));
        }
    }

    /** Professor cadastrado no meio do semestre recebe a cota dele na hora (HU05: todo professor tem a do semestre). */
    @Transactional
    public void creditar(Professor professor) {
        Instant agora = relogio.instant();
        String semestre = semestre(agora);
        if (!cotas.existsByProfessorIdAndSemestre(professor.getId(), semestre)) {
            professor.setSaldo(professor.getSaldo() + VALOR);
            cotas.save(new CotaSemestral(professor, semestre, VALOR, agora));
        }
    }

    private static String semestre(Instant instante) {
        ZonedDateTime data = instante.atZone(FUSO);
        return data.getYear() + (data.getMonthValue() <= 6 ? ".1" : ".2");
    }

}
