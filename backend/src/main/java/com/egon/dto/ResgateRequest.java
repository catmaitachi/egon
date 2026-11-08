package com.egon.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

/** HU09: qual vantagem do catálogo o aluno resgata. */
public record ResgateRequest(@NotNull(message = "Escolha a vantagem.") UUID vantagemId) {
}
