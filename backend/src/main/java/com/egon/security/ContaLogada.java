package com.egon.security;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import lombok.Getter;

/**
 * Quem está logado, guardado na sessão. O {@code id} é o da tabela do perfil e é o que as regras
 * de acesso comparam com o id do caminho ({@code #id == principal.id}).
 */
@Getter
public class ContaLogada implements UserDetails, CredentialsContainer {

    private final UUID id;
    private final Perfil perfil;
    private final String nome;
    private final String login;
    /** O hash, só até a senha ser conferida no login; depois é apagado. */
    private String senha;

    public ContaLogada(UUID id, Perfil perfil, String nome, String login, String senha) {
        this.id = id;
        this.perfil = perfil;
        this.nome = nome;
        this.login = login;
        this.senha = senha;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + perfil));
    }

    @Override
    public String getPassword() {
        return senha;
    }

    @Override
    public String getUsername() {
        return login;
    }

    @Override
    public void eraseCredentials() {
        senha = null;
    }

}
