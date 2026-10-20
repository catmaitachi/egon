package com.egon.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.egon.auth.UsuarioLogado;
import com.egon.dto.TransacaoRequest;
import com.egon.dto.TransacaoResponse;
import com.egon.service.TransacaoService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/** HU06: o professor envia moedas a um aluno. */
@RestController
@RequestMapping("/transacoes")
@RequiredArgsConstructor
public class TransacaoController {

    private final TransacaoService transacoes;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    TransacaoResponse enviar(UsuarioLogado usuario, @Valid @RequestBody TransacaoRequest requisicao) {
        return transacoes.enviar(usuario, requisicao);
    }

}
