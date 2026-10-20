package com.egon.dto;

import org.springframework.web.multipart.MultipartFile;

import com.egon.model.TipoVantagem;
import com.egon.validation.Imagem;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/** Cadastro de vantagem (HU08), enviado como multipart por causa da foto. */
public record VantagemForm(
        @NotBlank(message = "Informe o nome da vantagem.")
        @Size(max = 100, message = "Use até 100 caracteres.") String nome,

        @NotBlank(message = "Descreva o que está sendo oferecido.")
        @Size(max = 1000, message = "Use até 1.000 caracteres.") String descricao,

        @NotNull(message = "Informe o custo em moedas.")
        @Positive(message = "O custo deve ser um número inteiro maior que zero.") Integer custo,

        @NotNull(message = "Escolha o tipo da vantagem.") TipoVantagem tipo,

        @Imagem MultipartFile foto) {

    public VantagemForm {
        nome = Campos.aparar(nome);
        descricao = Campos.aparar(descricao);
    }

}
