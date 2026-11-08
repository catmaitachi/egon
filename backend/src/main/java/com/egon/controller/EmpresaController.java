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

import com.egon.dto.EmpresaAtualizacaoRequest;
import com.egon.dto.EmpresaCadastroRequest;
import com.egon.dto.EmpresaResponse;
import com.egon.service.EmpresaService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/empresas")
@RequiredArgsConstructor
public class EmpresaController {

    private final EmpresaService empresas;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    List<EmpresaResponse> listar() {
        return empresas.listar();
    }

    @PreAuthorize("hasRole('ADMIN') or #id == principal.id")
    @GetMapping("/{id}")
    EmpresaResponse buscar(@PathVariable UUID id) {
        return empresas.buscar(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    EmpresaResponse cadastrar(@Valid @RequestBody EmpresaCadastroRequest requisicao) {
        return empresas.cadastrar(requisicao);
    }

    @PreAuthorize("hasRole('ADMIN') or #id == principal.id")
    @PutMapping("/{id}")
    EmpresaResponse atualizar(@PathVariable UUID id, @Valid @RequestBody EmpresaAtualizacaoRequest requisicao) {
        return empresas.atualizar(id, requisicao);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void excluir(@PathVariable UUID id) {
        empresas.excluir(id);
    }

}
