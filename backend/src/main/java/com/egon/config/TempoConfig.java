package com.egon.config;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Relógio e agendamento. O relógio é um bean para o semestre da cota (HU05) poder ser testado
 * com uma data fixa, inclusive nas viradas de semestre.
 */
@Configuration
@EnableScheduling
public class TempoConfig {

    @Bean
    Clock relogio() {
        return Clock.systemUTC();
    }

}
