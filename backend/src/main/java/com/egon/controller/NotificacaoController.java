package com.egon.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.egon.auth.UsuarioLogado;
import com.egon.dto.NotificacaoResponse;
import com.egon.service.NotificacaoService;

import lombok.RequiredArgsConstructor;

/** Os e-mails recebidos por quem está logado (HU07, HU10), para ler também no painel. */
@RestController
@RequestMapping("/notificacoes")
@RequiredArgsConstructor
public class NotificacaoController {

    private final NotificacaoService notificacoes;

    @GetMapping
    List<NotificacaoResponse> listar(UsuarioLogado usuario) {
        return notificacoes.listar(usuario);
    }

}
