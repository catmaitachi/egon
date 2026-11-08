package com.egon.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.egon.dto.CupomResponse;
import com.egon.dto.ResgateRequest;
import com.egon.service.ResgateService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/** HU09 (resgate pelo aluno) e HU10 (cupom conferido e confirmado pela empresa). */
@RestController
@RequiredArgsConstructor
public class ResgateController {

    private final ResgateService resgates;

    @PreAuthorize("hasRole('ALUNO') and #id == principal.id")
    @PostMapping("/alunos/{id}/resgates")
    @ResponseStatus(HttpStatus.CREATED)
    CupomResponse resgatar(@PathVariable UUID id, @Valid @RequestBody ResgateRequest requisicao) {
        return resgates.resgatar(id, requisicao.vantagemId());
    }

    @PreAuthorize("hasRole('ALUNO') and #id == principal.id")
    @GetMapping("/alunos/{id}/cupons")
    List<CupomResponse> doAluno(@PathVariable UUID id) {
        return resgates.doAluno(id);
    }

    @PreAuthorize("hasRole('EMPRESA') and #id == principal.id")
    @GetMapping("/empresas/{id}/cupons")
    List<CupomResponse> daEmpresa(@PathVariable UUID id) {
        return resgates.daEmpresa(id);
    }

    @PreAuthorize("hasRole('EMPRESA') and #id == principal.id")
    @GetMapping("/empresas/{id}/cupons/{codigo}")
    CupomResponse buscar(@PathVariable UUID id, @PathVariable String codigo) {
        return resgates.buscar(id, codigo);
    }

    @PreAuthorize("hasRole('EMPRESA') and #id == principal.id")
    @PostMapping("/empresas/{id}/cupons/{codigo}/confirmar")
    CupomResponse confirmar(@PathVariable UUID id, @PathVariable String codigo) {
        return resgates.confirmar(id, codigo);
    }

}
