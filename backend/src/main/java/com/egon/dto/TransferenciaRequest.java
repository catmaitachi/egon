package com.egon.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/** Repasse de Egos a um colega, saindo da carteira com o professor escolhido. O remetente vem do caminho. */
public record TransferenciaRequest(
        @NotNull(message = "Escolha o colega.") UUID destinatarioId,
        @NotNull(message = "Escolha de qual professor saem os Egos.") UUID professorId,
        @NotNull(message = "Informe o valor.")
        @Positive(message = "O valor precisa ser maior que zero.") Integer valor,
        @NotBlank(message = "Escreva o motivo do repasse.")
        @Size(max = 500, message = "Use até 500 caracteres.") String motivo) {

    public TransferenciaRequest {
        motivo = Campos.aparar(motivo);
    }

}
