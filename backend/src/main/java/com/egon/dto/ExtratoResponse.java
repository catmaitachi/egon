package com.egon.dto;

import java.util.List;

/**
 * Saldo, carteiras por professor (só no do aluno; vazia no do professor) e movimentos, do mais
 * recente para o mais antigo (HU11, HU12).
 */
public record ExtratoResponse(int saldo, List<CarteiraResponse> carteiras, List<Movimento> movimentos) {
}
