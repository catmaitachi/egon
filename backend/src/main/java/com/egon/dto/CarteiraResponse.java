package com.egon.dto;

import com.egon.model.Carteira;

/** Quanto o aluno tem de cada professor, com as regras de repasse desses Egos. */
public record CarteiraResponse(PessoaResponse professor, int saldo, RegrasResponse regras) {

    public static CarteiraResponse de(Carteira carteira) {
        return new CarteiraResponse(PessoaResponse.de(carteira.getProfessor()), carteira.getSaldo(),
                RegrasResponse.de(carteira.getProfessor().getRegras()));
    }

}
