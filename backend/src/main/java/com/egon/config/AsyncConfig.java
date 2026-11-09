package com.egon.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/** Liga o {@code @Async}: os e-mails saem numa thread do executor do Spring, sem segurar a requisição. */
@Configuration
@EnableAsync
public class AsyncConfig {
}
