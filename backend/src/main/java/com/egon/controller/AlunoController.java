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

import com.egon.dto.AlunoAtualizacaoRequest;
import com.egon.dto.AlunoCadastroRequest;
import com.egon.dto.AlunoResponse;
import com.egon.dto.ColegaResponse;
import com.egon.dto.ExtratoResponse;
import com.egon.service.AlunoService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/alunos")
@RequiredArgsConstructor
public class AlunoController {

    private final AlunoService alunos;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    List<AlunoResponse> listar() {
        return alunos.listar();
    }

    @PreAuthorize("hasRole('ADMIN') or #id == principal.id")
    @GetMapping("/{id}")
    AlunoResponse buscar(@PathVariable UUID id) {
        return alunos.buscar(id);
    }

    @PreAuthorize("hasRole('ADMIN') or #id == principal.id")
    @GetMapping("/{id}/extrato")
    ExtratoResponse extrato(@PathVariable UUID id) {
        return alunos.extrato(id);
    }

    @PreAuthorize("hasRole('ALUNO') and #id == principal.id")
    @GetMapping("/{id}/colegas")
    List<ColegaResponse> colegas(@PathVariable UUID id) {
        return alunos.colegas(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    AlunoResponse cadastrar(@Valid @RequestBody AlunoCadastroRequest requisicao) {
        return alunos.cadastrar(requisicao);
    }

    @PreAuthorize("hasRole('ADMIN') or #id == principal.id")
    @PutMapping("/{id}")
    AlunoResponse atualizar(@PathVariable UUID id, @Valid @RequestBody AlunoAtualizacaoRequest requisicao) {
        return alunos.atualizar(id, requisicao);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void excluir(@PathVariable UUID id) {
        alunos.excluir(id);
    }

}
