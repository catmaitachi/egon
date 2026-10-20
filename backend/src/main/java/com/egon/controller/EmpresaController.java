package com.egon.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
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

    @GetMapping
    List<EmpresaResponse> listar() {
        return empresas.listar();
    }

    @GetMapping("/{cnpj}")
    EmpresaResponse buscar(@PathVariable String cnpj) {
        return empresas.buscar(cnpj);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    EmpresaResponse cadastrar(@Valid @RequestBody EmpresaCadastroRequest requisicao) {
        return empresas.cadastrar(requisicao);
    }

    @PutMapping("/{cnpj}")
    EmpresaResponse atualizar(@PathVariable String cnpj, @Valid @RequestBody EmpresaAtualizacaoRequest requisicao) {
        return empresas.atualizar(cnpj, requisicao);
    }

    @DeleteMapping("/{cnpj}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void excluir(@PathVariable String cnpj) {
        empresas.excluir(cnpj);
    }

}
