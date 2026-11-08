package com.egon.security;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.session.ChangeSessionIdAuthenticationStrategy;
import org.springframework.security.web.authentication.session.CompositeSessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfAuthenticationStrategy;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Component;

import com.egon.dto.SessaoResponse;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/** Abre a sessão de quem acabou de entrar: id de sessão novo (contra fixação) e token de CSRF novo. */
@Component
public class Sessoes {

    private final SessionAuthenticationStrategy aoEntrar = new CompositeSessionAuthenticationStrategy(List.of(
            new ChangeSessionIdAuthenticationStrategy(),
            new CsrfAuthenticationStrategy(CookieCsrfTokenRepository.withHttpOnlyFalse())));

    private final SecurityContextRepository contextos = new HttpSessionSecurityContextRepository();

    public SessaoResponse abrir(Authentication conta, HttpServletRequest http, HttpServletResponse resposta) {
        http.getSession();
        aoEntrar.onAuthentication(conta, http, resposta);
        // O token novo é preguiçoso: lido aqui, o cookie dele já sai na resposta do login.
        ((CsrfToken) http.getAttribute(CsrfToken.class.getName())).getToken();
        SecurityContext contexto = SecurityContextHolder.createEmptyContext();
        contexto.setAuthentication(conta);
        SecurityContextHolder.setContext(contexto);
        contextos.saveContext(contexto, http, resposta);
        return SessaoResponse.de((ContaLogada) conta.getPrincipal());
    }

}
