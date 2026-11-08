package com.egon.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/** As regras de repasse que o professor define para os Egos dele. Máximo e limite em branco: sem limite. */
public record RegrasRequest(
        @NotNull(message = "Diga se o repasse é permitido.") Boolean permiteRepasse,
        @NotNull(message = "Diga se o repasse precisa de aprovação.") Boolean exigeAprovacao,
        @Positive(message = "Use um valor maior que zero ou deixe em branco.") Integer valorMaximo,
        @Positive(message = "Use um valor maior que zero ou deixe em branco.") Integer limiteMensal,
        @NotNull(message = "Diga se vale só para o mesmo curso.") Boolean mesmoCurso) {
}
