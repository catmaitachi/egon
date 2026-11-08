package com.egon.controller;

import java.util.List;

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

import com.egon.dto.CursoRequest;
import com.egon.dto.CursoResponse;
import com.egon.service.CursoService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/** A lista é pública (cadastro de aluno); criar, editar e excluir é do admin. */
@RestController
@RequestMapping("/cursos")
@RequiredArgsConstructor
public class CursoController {

    private final CursoService cursos;

    @GetMapping
    List<CursoResponse> listar() {
        return cursos.listar();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    CursoResponse cadastrar(@Valid @RequestBody CursoRequest requisicao) {
        return cursos.cadastrar(requisicao);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    CursoResponse atualizar(@PathVariable Integer id, @Valid @RequestBody CursoRequest requisicao) {
        return cursos.atualizar(id, requisicao);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void excluir(@PathVariable Integer id) {
        cursos.excluir(id);
    }

}
