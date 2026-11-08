package com.egon.security;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

/**
 * Derruba a sessão de uma conta que deixou de existir: sem isto, quem foi excluído pelo admin
 * continuaria entrando até a sessão expirar. Não é bean de propósito, para o Spring Boot não
 * registrá-lo também como filtro solto, fora da cadeia de segurança.
 */
@RequiredArgsConstructor
class ContaAtivaFilter extends OncePerRequestFilter {

    private final ContasService contas;

    @Override
    protected void doFilterInternal(HttpServletRequest requisicao, HttpServletResponse resposta, FilterChain cadeia)
            throws ServletException, IOException {
        Authentication autenticacao = SecurityContextHolder.getContext().getAuthentication();
        if (autenticacao != null && autenticacao.getPrincipal() instanceof ContaLogada conta && !contas.existe(conta)) {
            SecurityContextHolder.clearContext();
            HttpSession sessao = requisicao.getSession(false);
            if (sessao != null) {
                sessao.invalidate();
            }
        }
        cadeia.doFilter(requisicao, resposta);
    }

}
