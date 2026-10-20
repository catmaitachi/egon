package com.egon.validation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.RECORD_COMPONENT;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

/** CNPJ já normalizado (numérico ou alfanumérico) com dígitos verificadores corretos. Vazio fica para o {@code @NotBlank}. */
@Retention(RUNTIME)
@Target({FIELD, PARAMETER, RECORD_COMPONENT})
@Constraint(validatedBy = CnpjValidator.class)
public @interface Cnpj {

    String message() default "CNPJ inválido. Confira os 14 caracteres.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
