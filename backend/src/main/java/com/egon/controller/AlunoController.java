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

import com.egon.dto.AlunoAtualizacaoRequest;
import com.egon.dto.AlunoCadastroRequest;
import com.egon.dto.AlunoResponse;
import com.egon.service.AlunoService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/alunos")
@RequiredArgsConstructor
public class AlunoController {

    private final AlunoService alunos;

    @GetMapping
    List<AlunoResponse> listar() {
        return alunos.listar();
    }

    @GetMapping("/{cpf}")
    AlunoResponse buscar(@PathVariable String cpf) {
        return alunos.buscar(cpf);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    AlunoResponse cadastrar(@Valid @RequestBody AlunoCadastroRequest requisicao) {
        return alunos.cadastrar(requisicao);
    }

    @PutMapping("/{cpf}")
    AlunoResponse atualizar(@PathVariable String cpf, @Valid @RequestBody AlunoAtualizacaoRequest requisicao) {
        return alunos.atualizar(cpf, requisicao);
    }

    @DeleteMapping("/{cpf}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void excluir(@PathVariable String cpf) {
        alunos.excluir(cpf);
    }

}
