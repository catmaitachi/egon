package com.egon.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.egon.auth.UsuarioLogado;
import com.egon.dto.LoginRequest;
import com.egon.dto.SessaoResponse;
import com.egon.service.AuthService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/** HU01. */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService auth;

    @PostMapping("/login")
    SessaoResponse entrar(@Valid @RequestBody LoginRequest requisicao) {
        return auth.entrar(requisicao);
    }

    /** Quem é o dono do token: a tela confere a sessão guardada ao abrir. */
    @GetMapping("/eu")
    SessaoResponse eu(UsuarioLogado usuario) {
        return auth.sessao(usuario);
    }

}
