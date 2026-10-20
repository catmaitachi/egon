package com.egon.validation;

import java.util.Locale;

/**
 * Normalização e dígitos verificadores de CPF e CNPJ.
 *
 * <p>O banco guarda só os caracteres do documento, sem máscara (CHAR(11) e CHAR(14) no
 * diagrama relacional): a máscara é apresentação e fica no front-end.
 */
public final class Documentos {

    private static final int[] PESOS_CPF_1 = {10, 9, 8, 7, 6, 5, 4, 3, 2};
    private static final int[] PESOS_CPF_2 = {11, 10, 9, 8, 7, 6, 5, 4, 3, 2};
    private static final int[] PESOS_CNPJ_1 = {5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
    private static final int[] PESOS_CNPJ_2 = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};

    private Documentos() {
    }

    public static String normalizarCpf(String cpf) {
        return cpf == null ? null : cpf.replaceAll("\\D", "");
    }

    /**
     * Mantém letras: desde julho de 2026 a Receita emite CNPJ alfanumérico (as 12 primeiras
     * posições aceitam A-Z). Os dois dígitos verificadores continuam numéricos.
     */
    public static String normalizarCnpj(String cnpj) {
        return cnpj == null ? null : cnpj.replaceAll("[^0-9A-Za-z]", "").toUpperCase(Locale.ROOT);
    }

    public static boolean cpfValido(String cpf) {
        if (cpf == null || !cpf.matches("\\d{11}") || repetido(cpf)) {
            return false;
        }
        return digito(cpf, PESOS_CPF_1) == cpf.charAt(9) - '0'
                && digito(cpf, PESOS_CPF_2) == cpf.charAt(10) - '0';
    }

    public static boolean cnpjValido(String cnpj) {
        if (cnpj == null || !cnpj.matches("[0-9A-Z]{12}\\d{2}") || repetido(cnpj)) {
            return false;
        }
        return digito(cnpj, PESOS_CNPJ_1) == cnpj.charAt(12) - '0'
                && digito(cnpj, PESOS_CNPJ_2) == cnpj.charAt(13) - '0';
    }

    /** "00000000000" e afins passam no cálculo, mas não são documentos. */
    private static boolean repetido(String documento) {
        return documento.chars().distinct().count() == 1;
    }

    /** Cada caractere vale seu código ASCII menos 48: dígitos valem 0-9 e letras, 17-42. */
    private static int digito(String documento, int[] pesos) {
        int soma = 0;
        for (int i = 0; i < pesos.length; i++) {
            soma += (documento.charAt(i) - '0') * pesos[i];
        }
        int resto = soma % 11;
        return resto < 2 ? 0 : 11 - resto;
    }

}
