package com.egon.dto;

import java.util.UUID;

import com.egon.model.Empresa;

/** A empresa como a API devolve: tudo menos a senha. */
public record EmpresaResponse(UUID id, String cnpj, String nome, String email, String login) {

    public static EmpresaResponse de(Empresa empresa) {
        return new EmpresaResponse(empresa.getId(), empresa.getCnpj(), empresa.getNome(), empresa.getEmail(), empresa.getLogin());
    }

}
