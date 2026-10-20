package com.egon.validation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.RECORD_COMPONENT;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

/**
 * Senha de 8 caracteres a 72 bytes. O teto é do BCrypt, que recusa senhas maiores; contar
 * bytes e não caracteres evita que uma senha acentuada passe aqui e quebre no hash.
 * Vazio fica para o {@code @NotBlank} (ou, na edição, significa manter a senha atual).
 */
@Retention(RUNTIME)
@Target({FIELD, PARAMETER, RECORD_COMPONENT})
@Constraint(validatedBy = SenhaValidator.class)
public @interface Senha {

    String message() default "Use de 8 a 72 caracteres (letras acentuadas contam como 2).";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
