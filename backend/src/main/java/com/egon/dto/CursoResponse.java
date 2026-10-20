package com.egon.dto;

import com.egon.model.Curso;

public record CursoResponse(Integer id, String nome) {

    public static CursoResponse de(Curso curso) {
        return new CursoResponse(curso.getId(), curso.getNome());
    }

}
