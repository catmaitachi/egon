package com.egon.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.egon.auth.UsuarioLogado;
import com.egon.dto.ResgateResponse;
import com.egon.dto.TrocaRequest;
import com.egon.dto.TrocaResponse;
import com.egon.service.TrocaService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/trocas")
@RequiredArgsConstructor
public class TrocaController {

    private final TrocaService trocas;

    /** HU09 e HU10: o aluno troca moedas por uma vantagem e recebe o cupom. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    TrocaResponse resgatar(UsuarioLogado usuario, @Valid @RequestBody TrocaRequest requisicao) {
        return trocas.resgatar(usuario, requisicao);
    }

    /** Os resgates das vantagens da empresa logada, com o código de cada cupom (HU10). */
    @GetMapping
    List<ResgateResponse> resgates(UsuarioLogado usuario) {
        return trocas.resgates(usuario);
    }

}
