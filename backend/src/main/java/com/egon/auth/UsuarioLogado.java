package com.egon.auth;

import com.egon.exception.ApiException;

/**
 * Quem fez a requisição, lido do token. Um parâmetro deste tipo num método de controller exige
 * login: sem token válido, a resposta é 401 ({@link UsuarioLogadoResolver}).
 *
 * @param id CPF do aluno ou do professor, ou CNPJ da empresa
 */
public record UsuarioLogado(Perfil perfil, String id) {

    /** Barra com 403 quem está logado mas tem outro perfil. */
    public String exigir(Perfil esperado) {
        if (perfil != esperado) {
            throw ApiException.acessoNegado();
        }
        return id;
    }

}
