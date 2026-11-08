package com.egon.dto;

import java.util.List;
import java.util.UUID;

/** O painel do admin: onde estão os Egos, se o reconhecimento acontece, por instituição e quem está no sistema. */
public record ResumoResponse(Egos egos, List<EnvioMes> enviosPorMes, List<PorInstituicao> porInstituicao,
        Contagens contagens) {

    /** Sempre vale: emitidos = comProfessores + comAlunos + reservados + resgatados. */
    public record Egos(long emitidos, long comProfessores, long comAlunos, long reservados, long resgatados) {
    }

    /** {@code mes} no formato "2026-11", no fuso de São Paulo. */
    public record EnvioMes(String mes, long quantidade, long valor) {
    }

    public record PorInstituicao(UUID id, String nome, long alunos, long professores, long enviado) {
    }

    public record Contagens(long instituicoes, long cursos, long alunos, long professores, long empresas,
            long vantagens) {
    }

}
