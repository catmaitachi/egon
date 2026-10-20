package com.egon.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class CnpjValidator implements ConstraintValidator<Cnpj, String> {

    @Override
    public boolean isValid(String cnpj, ConstraintValidatorContext context) {
        return cnpj == null || cnpj.isEmpty() || Documentos.cnpjValido(cnpj);
    }

}
