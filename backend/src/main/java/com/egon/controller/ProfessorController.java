package com.egon.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.egon.auth.UsuarioLogado;
import com.egon.dto.AlunoResumoResponse;
import com.egon.dto.ExtratoResponse;
import com.egon.dto.ProfessorResponse;
import com.egon.service.ProfessorService;

import lombok.RequiredArgsConstructor;

/** Painel do professor. Professores não se cadastram pela API: são pré-cadastrados (HU04). */
@RestController
@RequestMapping("/professores")
@RequiredArgsConstructor
public class ProfessorController {

    private final ProfessorService professores;

    @GetMapping("/eu")
    ProfessorResponse eu(UsuarioLogado usuario) {
        return professores.buscar(usuario);
    }

    /** HU06: alunos da instituição do professor, filtrados pelo nome. */
    @GetMapping("/eu/alunos")
    List<AlunoResumoResponse> alunos(UsuarioLogado usuario, @RequestParam(required = false) String busca) {
        return professores.alunosDaInstituicao(usuario, busca);
    }

    /** HU12. */
    @GetMapping("/eu/extrato")
    ExtratoResponse extrato(UsuarioLogado usuario) {
        return professores.extrato(usuario);
    }

}
