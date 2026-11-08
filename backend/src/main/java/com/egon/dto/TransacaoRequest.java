package com.egon.dto;

import java.util.UUID;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/** Envio de moedas de um professor a um aluno (HU06). O professor vem do caminho. */
public record TransacaoRequest(
        @NotNull(message = "Escolha o aluno.") UUID idAluno,

        @NotNull(message = "Informe o valor.")
        @Positive(message = "O valor precisa ser maior que zero.") Integer valor,

        @NotBlank(message = "Escreva o motivo do envio.")
        @Size(max = 500, message = "Use até 500 caracteres.") String motivo) {

    public TransacaoRequest {
        motivo = Campos.aparar(motivo);
    }

}
