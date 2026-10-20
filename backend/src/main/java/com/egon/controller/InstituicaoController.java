package com.egon.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.egon.dto.InstituicaoResponse;
import com.egon.service.InstituicaoService;

import lombok.RequiredArgsConstructor;

/** Só leitura: as instituições conveniadas são pré-cadastradas (HU04). */
@RestController
@RequestMapping("/instituicoes")
@RequiredArgsConstructor
public class InstituicaoController {

    private final InstituicaoService instituicoes;

    @GetMapping
    List<InstituicaoResponse> listar() {
        return instituicoes.listar();
    }

}
