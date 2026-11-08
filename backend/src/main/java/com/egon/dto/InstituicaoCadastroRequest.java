package com.egon.dto;

import com.egon.validation.Cnpj;
import com.egon.validation.Documentos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Instituição conveniada, cadastrada pelo admin a partir do convênio. */
public record InstituicaoCadastroRequest(
        @NotBlank(message = "Informe o CNPJ.") @Cnpj String cnpj,
        @NotBlank(message = "Informe o nome da instituição.")
        @Size(max = 100, message = "Use até 100 caracteres.") String nome,
        @NotBlank(message = "Informe o tipo.")
        @Size(max = 30, message = "Use até 30 caracteres.") String tipo) {

    public InstituicaoCadastroRequest {
        cnpj = Documentos.normalizarCnpj(cnpj);
        nome = Campos.aparar(nome);
        tipo = Campos.aparar(tipo);
    }

}
