package com.egon.dto;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

/** Saldo atual e lançamentos, dos mais recentes para os mais antigos (HU11, HU12). */
public record ExtratoResponse(int saldo, List<LancamentoResponse> lancamentos) {

    @SafeVarargs
    public static ExtratoResponse de(int saldo, List<LancamentoResponse>... grupos) {
        return new ExtratoResponse(saldo, Stream.of(grupos).flatMap(List::stream)
                .sorted(Comparator.comparing(LancamentoResponse::data).reversed())
                .toList());
    }

}
