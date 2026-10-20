package com.egon.config;

import java.time.Clock;
import java.time.ZoneId;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Relógio no fuso do sistema (egon.fuso). Datas de transações e trocas e a virada de semestre
 * (HU05) saem dele, e não do fuso da máquina onde a API roda.
 */
@Configuration
public class RelogioConfig {

    @Bean
    Clock relogio(@Value("${egon.fuso}") String fuso) {
        return Clock.system(ZoneId.of(fuso));
    }

}
