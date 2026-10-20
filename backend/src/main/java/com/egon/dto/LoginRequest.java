package com.egon.dto;

import jakarta.validation.constraints.NotBlank;

/** HU01. O login é comparado em minúsculas, como é guardado no cadastro. */
public record LoginRequest(
        @NotBlank(message = "Informe o login.") String login,
        @NotBlank(message = "Informe a senha.") String senha) {

    public LoginRequest {
        login = Campos.minusculo(login);
    }

}
