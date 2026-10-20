package com.egon.service;

import java.security.SecureRandom;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.egon.model.Cupom;
import com.egon.model.Troca;
import com.egon.repository.CupomRepository;

import lombok.RequiredArgsConstructor;

/** HU10: código alfanumérico único para cada troca. */
@Service
@RequiredArgsConstructor
public class CupomService {

    /** Sem 0/O e 1/I, que se confundem quando o aluno lê o código em voz alta no balcão. */
    private static final String ALFABETO = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int TAMANHO = 10;

    private final SecureRandom aleatorio = new SecureRandom();
    private final CupomRepository cupons;

    @Transactional(propagation = Propagation.MANDATORY)
    public Cupom gerar(Troca troca) {
        String codigo;
        do {
            codigo = novoCodigo();
        } while (cupons.existsById(codigo));
        Cupom cupom = cupons.save(new Cupom(codigo, troca));
        troca.setCupom(cupom);
        return cupom;
    }

    /** "ABCDE-23456": dois blocos de cinco, mais fáceis de conferir. */
    private String novoCodigo() {
        StringBuilder codigo = new StringBuilder();
        for (int i = 0; i < TAMANHO; i++) {
            if (i == TAMANHO / 2) {
                codigo.append('-');
            }
            codigo.append(ALFABETO.charAt(aleatorio.nextInt(ALFABETO.length())));
        }
        return codigo.toString();
    }

}
