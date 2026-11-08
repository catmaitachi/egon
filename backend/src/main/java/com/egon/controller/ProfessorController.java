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

import com.egon.dto.ColegaResponse;
import com.egon.dto.ExtratoResponse;
import com.egon.dto.ProfessorAtualizacaoRequest;
import com.egon.dto.ProfessorCadastroRequest;
import com.egon.dto.ProfessorResponse;
import com.egon.dto.TransacaoRequest;
import com.egon.dto.RegrasRequest;
import com.egon.dto.RegrasResponse;
import com.egon.dto.TransacaoResponse;
import com.egon.dto.TransferenciaResponse;
import com.egon.service.ProfessorService;
import com.egon.service.TransacaoService;
import com.egon.service.TransferenciaService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/professores")
@RequiredArgsConstructor
public class ProfessorController {

    private final ProfessorService professores;
    private final TransacaoService transacoes;
    private final TransferenciaService transferencias;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    List<ProfessorResponse> listar() {
        return professores.listar();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    ProfessorResponse cadastrar(@Valid @RequestBody ProfessorCadastroRequest requisicao) {
        return professores.cadastrar(requisicao);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    ProfessorResponse atualizar(@PathVariable UUID id, @Valid @RequestBody ProfessorAtualizacaoRequest requisicao) {
        return professores.atualizar(id, requisicao);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void excluir(@PathVariable UUID id) {
        professores.excluir(id);
    }

    @PreAuthorize("hasRole('ADMIN') or #id == principal.id")
    @GetMapping("/{id}")
    ProfessorResponse buscar(@PathVariable UUID id) {
        return professores.buscar(id);
    }

    @PreAuthorize("hasRole('ADMIN') or #id == principal.id")
    @GetMapping("/{id}/extrato")
    ExtratoResponse extrato(@PathVariable UUID id) {
        return professores.extrato(id);
    }

    @PreAuthorize("hasRole('PROFESSOR') and #id == principal.id")
    @GetMapping("/{id}/alunos")
    List<ColegaResponse> alunos(@PathVariable UUID id) {
        return professores.alunos(id);
    }

    @PreAuthorize("hasRole('PROFESSOR') and #id == principal.id")
    @GetMapping("/{id}/regras")
    RegrasResponse regras(@PathVariable UUID id) {
        return transferencias.regras(id);
    }

    @PreAuthorize("hasRole('PROFESSOR') and #id == principal.id")
    @PutMapping("/{id}/regras")
    RegrasResponse definirRegras(@PathVariable UUID id, @Valid @RequestBody RegrasRequest requisicao) {
        return transferencias.definirRegras(id, requisicao);
    }

    @PreAuthorize("hasRole('PROFESSOR') and #id == principal.id")
    @GetMapping("/{id}/transferencias")
    List<TransferenciaResponse> transferencias(@PathVariable UUID id) {
        return transferencias.doProfessor(id);
    }

    @PreAuthorize("hasRole('PROFESSOR') and #id == principal.id")
    @PostMapping("/{id}/transacoes")
    @ResponseStatus(HttpStatus.CREATED)
    TransacaoResponse enviar(@PathVariable UUID id, @Valid @RequestBody TransacaoRequest requisicao) {
        return transacoes.enviar(id, requisicao);
    }

}
