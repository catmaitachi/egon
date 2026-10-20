package com.egon.dto;

import com.egon.model.Usuario;
import com.egon.validation.Cnpj;
import com.egon.validation.Cpf;
import com.egon.validation.Documentos;
import com.egon.validation.Senha;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Cadastro público de aluno (HU02). Instituição e curso vêm das listas da API. */
public record AlunoCadastroRequest(
        @NotBlank(message = "Informe o nome completo.")
        @Size(max = 100, message = "Use até 100 caracteres.") String nome,

        @NotBlank(message = "Informe o e-mail.")
        @Email(message = "E-mail inválido.")
        @Size(max = 100, message = "Use até 100 caracteres.") String email,

        @NotBlank(message = "Informe o CPF.") @Cpf String cpf,

        @NotBlank(message = "Informe o RG.")
        @Size(max = 20, message = "Use até 20 caracteres.") String rg,

        @NotBlank(message = "Informe o endereço.")
        @Size(max = 255, message = "Use até 255 caracteres.") String endereco,

        @NotBlank(message = "Escolha a instituição.") @Cnpj String cnpjInstituicao,

        @NotNull(message = "Escolha o curso.") Integer idCurso,

        @NotBlank(message = "Escolha um login.")
        @Pattern(regexp = Usuario.LOGIN_FORMATO,
                message = "Use de 3 a 50 letras minúsculas, números, ponto, hífen ou sublinhado.") String login,

        @NotBlank(message = "Crie uma senha.") @Senha String senha) {

    public AlunoCadastroRequest {
        nome = Campos.aparar(nome);
        email = Campos.minusculo(email);
        cpf = Documentos.normalizarCpf(cpf);
        rg = Campos.aparar(rg);
        endereco = Campos.aparar(endereco);
        cnpjInstituicao = Documentos.normalizarCnpj(cnpjInstituicao);
        login = Campos.minusculo(login);
    }

}
