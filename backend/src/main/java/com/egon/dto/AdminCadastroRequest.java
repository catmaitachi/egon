package com.egon.dto;

import com.egon.model.Usuario;
import com.egon.validation.Senha;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AdminCadastroRequest(
        @NotBlank(message = "Informe o nome.")
        @Size(max = 100, message = "Use até 100 caracteres.") String nome,
        @NotBlank(message = "Escolha um login.")
        @Pattern(regexp = Usuario.LOGIN_FORMATO,
                message = "Use de 3 a 50 letras minúsculas, números, ponto, hífen ou sublinhado.") String login,
        @NotBlank(message = "Crie uma senha.") @Senha String senha) {

    public AdminCadastroRequest {
        nome = Campos.aparar(nome);
        login = Campos.minusculo(login);
    }

}
