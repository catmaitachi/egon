package com.egon;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/** {@code @EnableScheduling}: a cota semestral dos professores roda sozinha (HU05). */
@SpringBootApplication
@EnableScheduling
public class EgonApplication {

    public static void main(String[] args) {
        SpringApplication.run(EgonApplication.class, args);
    }

}
