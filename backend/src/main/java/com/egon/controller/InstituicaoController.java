package com.egon.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.egon.dto.InstituicaoAtualizacaoRequest;
import com.egon.dto.InstituicaoCadastroRequest;
import com.egon.dto.InstituicaoResponse;
import com.egon.service.InstituicaoService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/** A lista é pública (cadastro de aluno); criar, editar e excluir é do admin. */
@RestController
@RequestMapping("/instituicoes")
@RequiredArgsConstructor
public class InstituicaoController {

    private final InstituicaoService instituicoes;

    @GetMapping
    List<InstituicaoResponse> listar() {
        return instituicoes.listar();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    InstituicaoResponse cadastrar(@Valid @RequestBody InstituicaoCadastroRequest requisicao) {
        return instituicoes.cadastrar(requisicao);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    InstituicaoResponse atualizar(@PathVariable UUID id, @Valid @RequestBody InstituicaoAtualizacaoRequest requisicao) {
        return instituicoes.atualizar(id, requisicao);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void excluir(@PathVariable UUID id) {
        instituicoes.excluir(id);
    }

}
