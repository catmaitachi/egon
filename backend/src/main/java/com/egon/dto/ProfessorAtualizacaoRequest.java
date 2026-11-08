package com.egon.dto;

import java.util.UUID;

import com.egon.model.Usuario;
import com.egon.validation.Senha;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Edição de professor pelo admin. Sem CPF; senha em branco mantém a atual. */
public record ProfessorAtualizacaoRequest(
        @NotBlank(message = "Informe o nome completo.")
        @Size(max = 100, message = "Use até 100 caracteres.") String nome,
        @NotBlank(message = "Informe o departamento.")
        @Size(max = 100, message = "Use até 100 caracteres.") String departamento,
        @NotNull(message = "Escolha a instituição.") UUID idInstituicao,
        @NotBlank(message = "Escolha um login.")
        @Pattern(regexp = Usuario.LOGIN_FORMATO,
                message = "Use de 3 a 50 letras minúsculas, números, ponto, hífen ou sublinhado.") String login,
        @Senha String senha) {

    public ProfessorAtualizacaoRequest {
        nome = Campos.aparar(nome);
        departamento = Campos.aparar(departamento);
        login = Campos.minusculo(login);
        senha = Campos.vazioComoNulo(senha);
    }

}
