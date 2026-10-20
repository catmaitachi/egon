package com.egon.dto;

import jakarta.validation.constraints.NotNull;

/** HU09: o aluno escolhe uma vantagem do catálogo. */
public record TrocaRequest(@NotNull(message = "Escolha uma vantagem.") Integer idVantagem) {
}
