package com.egon.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.egon.dto.CursoResponse;
import com.egon.service.CursoService;

import lombok.RequiredArgsConstructor;

/** Só leitura: o aluno escolhe o curso numa lista. */
@RestController
@RequestMapping("/cursos")
@RequiredArgsConstructor
public class CursoController {

    private final CursoService cursos;

    @GetMapping
    List<CursoResponse> listar() {
        return cursos.listar();
    }

}
