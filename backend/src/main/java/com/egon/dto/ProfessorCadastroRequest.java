package com.egon.dto;

import java.util.UUID;

import com.egon.model.Usuario;
import com.egon.validation.Cpf;
import com.egon.validation.Documentos;
import com.egon.validation.Senha;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Pré-cadastro de professor pelo admin, a partir da lista da instituição (HU04). */
public record ProfessorCadastroRequest(
        @NotBlank(message = "Informe o CPF.") @Cpf String cpf,
        @NotBlank(message = "Informe o nome completo.")
        @Size(max = 100, message = "Use até 100 caracteres.") String nome,
        @NotBlank(message = "Informe o e-mail.")
        @Email(regexp = Campos.EMAIL_FORMATO, message = "E-mail inválido.")
        @Size(max = 100, message = "Use até 100 caracteres.") String email,
        @NotBlank(message = "Informe o departamento.")
        @Size(max = 100, message = "Use até 100 caracteres.") String departamento,
        @NotNull(message = "Escolha a instituição.") UUID idInstituicao,
        @NotBlank(message = "Escolha um login.")
        @Pattern(regexp = Usuario.LOGIN_FORMATO,
                message = "Use de 3 a 50 letras minúsculas, números, ponto, hífen ou sublinhado.") String login,
        @NotBlank(message = "Crie a senha inicial.") @Senha String senha) {

    public ProfessorCadastroRequest {
        cpf = Documentos.normalizarCpf(cpf);
        nome = Campos.aparar(nome);
        email = Campos.minusculo(email);
        departamento = Campos.aparar(departamento);
        login = Campos.minusculo(login);
    }

}
