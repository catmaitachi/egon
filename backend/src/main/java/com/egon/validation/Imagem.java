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
 * Arquivo obrigatório e de imagem (JPG, PNG, WebP ou GIF), conferido pelos primeiros bytes:
 * o tipo informado pelo navegador e a extensão do nome são escolha de quem envia.
 */
@Retention(RUNTIME)
@Target({FIELD, PARAMETER, RECORD_COMPONENT})
@Constraint(validatedBy = ImagemValidator.class)
public @interface Imagem {

    String message() default "Use uma imagem JPG, PNG, WebP ou GIF.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
