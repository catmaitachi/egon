package com.egon.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.egon.dto.TransferenciaRequest;
import com.egon.dto.TransferenciaResponse;
import com.egon.model.StatusTransferencia;
import com.egon.security.ContaLogada;
import com.egon.service.TransferenciaService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/** Repasse de Egos entre alunos. Quem decide cada transferência é conferido no serviço, que conhece a origem. */
@RestController
@RequiredArgsConstructor
public class TransferenciaController {

    private final TransferenciaService transferencias;

    @PreAuthorize("hasRole('ALUNO') and #id == principal.id")
    @PostMapping("/alunos/{id}/transferencias")
    @ResponseStatus(HttpStatus.CREATED)
    TransferenciaResponse pedir(@PathVariable UUID id, @Valid @RequestBody TransferenciaRequest requisicao) {
        return transferencias.pedir(id, requisicao);
    }

    @PreAuthorize("hasRole('PROFESSOR')")
    @PostMapping("/transferencias/{id}/aprovar")
    TransferenciaResponse aprovar(@PathVariable UUID id, @AuthenticationPrincipal ContaLogada quem) {
        return transferencias.decidir(id, StatusTransferencia.APROVADA, quem);
    }

    @PreAuthorize("hasRole('PROFESSOR')")
    @PostMapping("/transferencias/{id}/recusar")
    TransferenciaResponse recusar(@PathVariable UUID id, @AuthenticationPrincipal ContaLogada quem) {
        return transferencias.decidir(id, StatusTransferencia.RECUSADA, quem);
    }

    @PreAuthorize("hasRole('ALUNO')")
    @PostMapping("/transferencias/{id}/cancelar")
    TransferenciaResponse cancelar(@PathVariable UUID id, @AuthenticationPrincipal ContaLogada quem) {
        return transferencias.decidir(id, StatusTransferencia.CANCELADA, quem);
    }

}
