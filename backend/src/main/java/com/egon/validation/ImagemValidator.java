package com.egon.validation;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Optional;

import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ImagemValidator implements ConstraintValidator<Imagem, MultipartFile> {

    @Override
    public boolean isValid(MultipartFile arquivo, ConstraintValidatorContext context) {
        if (arquivo == null || arquivo.isEmpty()) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Escolha uma foto.").addConstraintViolation();
            return false;
        }
        return formato(arquivo).isPresent();
    }

    /** Extensão do arquivo de imagem, pelos primeiros bytes; vazio se não for imagem aceita. */
    public static Optional<String> formato(MultipartFile arquivo) {
        byte[] inicio;
        try (InputStream entrada = arquivo.getInputStream()) {
            inicio = entrada.readNBytes(12);
        } catch (IOException e) {
            return Optional.empty();
        }
        if (comeca(inicio, 0xFF, 0xD8, 0xFF)) {
            return Optional.of("jpg");
        }
        if (comeca(inicio, 0x89, 'P', 'N', 'G')) {
            return Optional.of("png");
        }
        if (comeca(inicio, 'G', 'I', 'F', '8')) {
            return Optional.of("gif");
        }
        if (comeca(inicio, 'R', 'I', 'F', 'F') && inicio.length >= 12
                && Arrays.equals(Arrays.copyOfRange(inicio, 8, 12), new byte[] {'W', 'E', 'B', 'P'})) {
            return Optional.of("webp");
        }
        return Optional.empty();
    }

    private static boolean comeca(byte[] dados, int... assinatura) {
        if (dados.length < assinatura.length) {
            return false;
        }
        for (int i = 0; i < assinatura.length; i++) {
            if ((dados[i] & 0xFF) != assinatura[i]) {
                return false;
            }
        }
        return true;
    }

}
