package com.egon.dto;

import com.egon.model.TipoVantagem;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/** Campos de texto do formulário de vantagem (HU08); a foto vem à parte, no multipart. */
public record VantagemRequest(
        @NotBlank(message = "Informe o nome da vantagem.")
        @Size(max = 100, message = "Use até 100 caracteres.") String nome,
        @NotBlank(message = "Descreva a vantagem.")
        @Size(max = 1000, message = "Use até 1.000 caracteres.") String descricao,
        @NotNull(message = "Informe o custo.")
        @Positive(message = "O custo precisa ser maior que zero.") Integer custo,
        @NotNull(message = "Escolha o tipo.") TipoVantagem tipo) {

    public VantagemRequest {
        nome = Campos.aparar(nome);
        descricao = Campos.aparar(descricao);
    }

}
