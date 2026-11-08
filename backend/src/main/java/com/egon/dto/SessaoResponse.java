package com.egon.dto;

import java.util.UUID;

import com.egon.security.ContaLogada;
import com.egon.security.Perfil;

/** Quem está logado: o front decide a tela inicial pelo perfil. */
public record SessaoResponse(Perfil perfil, UUID id, String nome) {

    public static SessaoResponse de(ContaLogada conta) {
        return new SessaoResponse(conta.getPerfil(), conta.getId(), conta.getNome());
    }

}
