package com.egon.dto;

import java.time.LocalDateTime;
import java.util.UUID;

/** O envio registrado, com o saldo que sobrou para o professor. */
public record TransacaoResponse(UUID id, LocalDateTime data, int valor, String motivo, String aluno, int saldoProfessor) {
}
