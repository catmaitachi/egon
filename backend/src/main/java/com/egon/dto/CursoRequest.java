package com.egon.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Cadastro e edição de curso pelo admin. */
public record CursoRequest(
        @NotBlank(message = "Informe o nome do curso.")
        @Size(max = 100, message = "Use até 100 caracteres.") String nome) {

    public CursoRequest {
        nome = Campos.aparar(nome);
    }

}
