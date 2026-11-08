package com.egon.service;

import java.util.List;

import com.egon.dto.CupomResponse;
import com.egon.service.TransferenciaNotificada.Aviso;

/** Um cupom foi emitido ou usado; o aviso sai só depois do commit, para cada pessoa da lista. */
public record CupomNotificado(String evento, CupomResponse cupom, List<Aviso> avisos) {
}
