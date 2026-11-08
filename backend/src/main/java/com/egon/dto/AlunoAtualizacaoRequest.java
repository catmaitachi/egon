package com.egon.dto;

import java.util.UUID;

import com.egon.model.Usuario;
import com.egon.validation.Documentos;
import com.egon.validation.Senha;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Edição de aluno. Não tem CPF: ele identifica a pessoa e não muda. A senha é opcional;
 * vazia, a atual continua valendo.
 */
public record AlunoAtualizacaoRequest(
        @NotBlank(message = "Informe o nome completo.")
        @Size(max = 100, message = "Use até 100 caracteres.") String nome,

        @NotBlank(message = "Informe o e-mail.")
        @Email(regexp = Campos.EMAIL_FORMATO, message = "E-mail inválido.")
        @Size(max = 100, message = "Use até 100 caracteres.") String email,

        @NotBlank(message = "Informe o RG.")
        @Size(max = 20, message = "Use até 20 caracteres.") String rg,

        @NotBlank(message = "Informe o endereço.")
        @Size(max = 255, message = "Use até 255 caracteres.") String endereco,

        @NotNull(message = "Escolha a instituição.") UUID idInstituicao,

        @NotNull(message = "Escolha o curso.") Integer idCurso,

        @NotBlank(message = "Escolha um login.")
        @Pattern(regexp = Usuario.LOGIN_FORMATO,
                message = "Use de 3 a 50 letras minúsculas, números, ponto, hífen ou sublinhado.") String login,

        @Senha String senha) {

    public AlunoAtualizacaoRequest {
        nome = Campos.aparar(nome);
        email = Campos.minusculo(email);
        rg = Campos.aparar(rg);
        endereco = Campos.aparar(endereco);
        login = Campos.minusculo(login);
        senha = Campos.vazioComoNulo(senha);
    }

}
