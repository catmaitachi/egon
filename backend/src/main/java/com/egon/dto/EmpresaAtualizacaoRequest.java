package com.egon.dto;

import com.egon.model.Usuario;
import com.egon.validation.Senha;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Edição de empresa. Não tem CNPJ: ele é a chave primária e não muda. A senha é opcional;
 * vazia, a atual continua valendo.
 */
public record EmpresaAtualizacaoRequest(
        @NotBlank(message = "Informe o nome da empresa.")
        @Size(max = 100, message = "Use até 100 caracteres.") String nome,

        @NotBlank(message = "Informe o e-mail de contato.")
        @Email(regexp = Campos.EMAIL_FORMATO, message = "E-mail inválido.")
        @Size(max = 100, message = "Use até 100 caracteres.") String email,

        @NotBlank(message = "Escolha um login.")
        @Pattern(regexp = Usuario.LOGIN_FORMATO,
                message = "Use de 3 a 50 letras minúsculas, números, ponto, hífen ou sublinhado.") String login,

        @Senha String senha) {

    public EmpresaAtualizacaoRequest {
        nome = Campos.aparar(nome);
        email = Campos.minusculo(email);
        login = Campos.minusculo(login);
        senha = Campos.vazioComoNulo(senha);
    }

}
