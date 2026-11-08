package com.egon.dto;

import com.egon.model.RegrasMoeda;

public record RegrasResponse(
        boolean permiteRepasse,
        boolean exigeAprovacao,
        Integer valorMaximo,
        Integer limiteMensal,
        boolean mesmoCurso) {

    public static RegrasResponse de(RegrasMoeda regras) {
        return new RegrasResponse(regras.isPermiteRepasse(), regras.isExigeAprovacao(), regras.getValorMaximo(),
                regras.getLimiteMensal(), regras.isMesmoCurso());
    }

}
