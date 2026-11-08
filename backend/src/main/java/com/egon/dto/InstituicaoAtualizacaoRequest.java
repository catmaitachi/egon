package com.egon.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Edição de instituição. Sem CNPJ: ele identifica a instituição e não muda. */
public record InstituicaoAtualizacaoRequest(
        @NotBlank(message = "Informe o nome da instituição.")
        @Size(max = 100, message = "Use até 100 caracteres.") String nome,
        @NotBlank(message = "Informe o tipo.")
        @Size(max = 30, message = "Use até 30 caracteres.") String tipo) {

    public InstituicaoAtualizacaoRequest {
        nome = Campos.aparar(nome);
        tipo = Campos.aparar(tipo);
    }

}
