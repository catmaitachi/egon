package com.egon.dto;

import com.egon.model.Usuario;
import com.egon.validation.Cnpj;
import com.egon.validation.Documentos;
import com.egon.validation.Senha;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Cadastro público de empresa parceira (HU03). */
public record EmpresaCadastroRequest(
        @NotBlank(message = "Informe o nome da empresa.")
        @Size(max = 100, message = "Use até 100 caracteres.") String nome,

        @NotBlank(message = "Informe o CNPJ.") @Cnpj String cnpj,

        @NotBlank(message = "Informe o e-mail de contato.")
        @Email(regexp = Campos.EMAIL_FORMATO, message = "E-mail inválido.")
        @Size(max = 100, message = "Use até 100 caracteres.") String email,

        @NotBlank(message = "Escolha um login.")
        @Pattern(regexp = Usuario.LOGIN_FORMATO,
                message = "Use de 3 a 50 letras minúsculas, números, ponto, hífen ou sublinhado.") String login,

        @NotBlank(message = "Crie uma senha.") @Senha String senha) {

    public EmpresaCadastroRequest {
        nome = Campos.aparar(nome);
        cnpj = Documentos.normalizarCnpj(cnpj);
        email = Campos.minusculo(email);
        login = Campos.minusculo(login);
    }

}
