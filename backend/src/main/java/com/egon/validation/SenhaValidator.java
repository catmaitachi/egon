package com.egon.validation;

import java.nio.charset.StandardCharsets;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class SenhaValidator implements ConstraintValidator<Senha, String> {

    static final int MINIMO_CARACTERES = 8;
    static final int MAXIMO_BYTES = 72;

    @Override
    public boolean isValid(String senha, ConstraintValidatorContext context) {
        if (senha == null || senha.isEmpty()) {
            return true;
        }
        return senha.length() >= MINIMO_CARACTERES
                && senha.getBytes(StandardCharsets.UTF_8).length <= MAXIMO_BYTES;
    }

}
