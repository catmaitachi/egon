package com.egon.service;

import java.util.List;
import java.util.UUID;

import com.egon.dto.TransferenciaResponse;
import com.egon.security.Perfil;

/**
 * Uma transferência mudou de estado. Leva quem deve ser avisado e o saldo de cada um, lidos ainda
 * dentro da transação; o aviso sai só depois do commit.
 */
public record TransferenciaNotificada(String evento, TransferenciaResponse transferencia, List<Aviso> avisos) {

    public record Aviso(Perfil perfil, UUID id, int saldo) {
    }

}
