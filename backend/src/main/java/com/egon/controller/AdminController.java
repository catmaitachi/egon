package com.egon.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.egon.dto.AdminAtualizacaoRequest;
import com.egon.dto.AdminCadastroRequest;
import com.egon.dto.AdminResponse;
import com.egon.security.ContaLogada;
import com.egon.service.AdminService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/admins")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService admins;

    @GetMapping
    List<AdminResponse> listar() {
        return admins.listar();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    AdminResponse cadastrar(@Valid @RequestBody AdminCadastroRequest requisicao) {
        return admins.cadastrar(requisicao);
    }

    @PutMapping("/{id}")
    AdminResponse atualizar(@PathVariable UUID id, @Valid @RequestBody AdminAtualizacaoRequest requisicao) {
        return admins.atualizar(id, requisicao);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void excluir(@PathVariable UUID id, @AuthenticationPrincipal ContaLogada conta) {
        admins.excluir(id, conta.getId());
    }

}
