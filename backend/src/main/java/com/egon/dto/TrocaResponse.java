package com.egon.dto;

import java.time.LocalDateTime;
import java.util.UUID;

/** A troca feita, com o cupom para apresentar na empresa e o saldo que sobrou (HU09, HU10). */
public record TrocaResponse(
        UUID id,
        LocalDateTime data,
        int valor,
        String codigoCupom,
        String vantagem,
        String empresa,
        int saldoAluno) {
}
