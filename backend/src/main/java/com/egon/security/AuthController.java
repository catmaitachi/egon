package com.egon.security;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.egon.dto.LoginRequest;
import com.egon.dto.SessaoResponse;
import com.egon.exception.ApiException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/** HU01: uma tela de login para os quatro perfis. A saída ({@code POST /auth/logout}) é do Spring Security. */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
class AuthController {

    private final AuthenticationManager autenticacao;
    private final Sessoes sessoes;

    /** Login inexistente e senha errada têm a mesma resposta: a tela não diz qual dos dois errou. */
    @PostMapping("/login")
    SessaoResponse entrar(@Valid @RequestBody LoginRequest requisicao, HttpServletRequest http, HttpServletResponse resposta) {
        Authentication conta;
        try {
            conta = autenticacao.authenticate(
                    UsernamePasswordAuthenticationToken.unauthenticated(requisicao.login(), requisicao.senha()));
        } catch (AuthenticationException e) {
            throw ApiException.credenciaisInvalidas();
        }
        return sessoes.abrir(conta, http, resposta);
    }

    /** Quem está logado. Ler o token entrega o cookie de CSRF, que o front precisa antes da primeira escrita. */
    @GetMapping("/eu")
    SessaoResponse eu(@AuthenticationPrincipal ContaLogada conta, CsrfToken token) {
        token.getToken();
        if (conta == null) {
            throw ApiException.naoAutenticado();
        }
        return SessaoResponse.de(conta);
    }

}
