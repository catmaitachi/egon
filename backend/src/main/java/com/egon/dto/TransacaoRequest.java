package com.egon.dto;

import com.egon.validation.Documentos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/** HU06: valor positivo e justificativa obrigatória. */
public record TransacaoRequest(
        @NotBlank(message = "Escolha o aluno.") String cpfAluno,

        @NotNull(message = "Informe quantas moedas enviar.")
        @Positive(message = "Envie pelo menos 1 moeda.") Integer valor,

        @NotBlank(message = "Escreva o motivo do reconhecimento.")
        @Size(max = 500, message = "Use até 500 caracteres.") String motivo) {

    public TransacaoRequest {
        cpfAluno = Documentos.normalizarCpf(cpfAluno);
        motivo = Campos.aparar(motivo);
    }

}
