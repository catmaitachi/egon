package com.egon.dto;

import com.egon.auth.Perfil;

/**
 * Quem está logado. O {@code token} só vem na resposta do login; a tela o manda de volta no
 * cabeçalho {@code Authorization: Bearer} de cada requisição.
 *
 * @param id CPF do aluno ou do professor, ou CNPJ da empresa
 */
public record SessaoResponse(String token, Perfil perfil, String id, String nome) {

    public SessaoResponse semToken() {
        return new SessaoResponse(null, perfil, id, nome);
    }

}
