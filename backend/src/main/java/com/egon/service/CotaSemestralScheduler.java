package com.egon.service;

import java.time.Clock;
import java.time.LocalDate;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * HU05: credita a cota dos professores a cada virada de semestre letivo (1º de janeiro e 1º de
 * julho). Roda também na subida da aplicação, para um semestre que virou com o servidor
 * desligado não ficar sem crédito; o {@link ProfessorService} garante um crédito só por semestre.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CotaSemestralScheduler {

    private final ProfessorService professores;
    private final Clock relogio;

    @EventListener(ApplicationReadyEvent.class)
    @Scheduled(cron = "0 0 0 1 1,7 *", zone = "${egon.fuso}")
    public void creditarSemestreAtual() {
        String semestre = semestre(LocalDate.now(relogio));
        int creditados = professores.creditarCotaSemestral(semestre);
        if (creditados > 0) {
            log.info("Cota de {} moedas do semestre {} creditada a {} professor(es).",
                    ProfessorService.COTA_SEMESTRAL, semestre, creditados);
        }
    }

    /** "2026.1" de janeiro a junho, "2026.2" de julho a dezembro. */
    public static String semestre(LocalDate data) {
        return data.getYear() + "." + (data.getMonthValue() <= 6 ? 1 : 2);
    }

}
