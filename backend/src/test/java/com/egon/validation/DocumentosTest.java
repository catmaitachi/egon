package com.egon.validation;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DocumentosTest {

    @Test
    @DisplayName("aceita CPF com dígitos verificadores corretos, com ou sem máscara")
    void aceitaCpfValido() {
        assertThat(Documentos.cpfValido("52998224725")).isTrue();
        assertThat(Documentos.cpfValido(Documentos.normalizarCpf("529.982.247-25"))).isTrue();
    }

    @Test
    @DisplayName("recusa CPF com dígito errado, tamanho errado ou todos os dígitos iguais")
    void recusaCpfInvalido() {
        assertThat(Documentos.cpfValido("52998224724")).isFalse();
        assertThat(Documentos.cpfValido("5299822472")).isFalse();
        assertThat(Documentos.cpfValido("11111111111")).isFalse();
        assertThat(Documentos.cpfValido(null)).isFalse();
    }

    @Test
    @DisplayName("aceita CNPJ numérico e alfanumérico, com ou sem máscara")
    void aceitaCnpjValido() {
        assertThat(Documentos.cnpjValido("11222333000181")).isTrue();
        // Exemplo publicado pela Receita para o CNPJ alfanumérico.
        assertThat(Documentos.normalizarCnpj("12.abc.345/01de-35")).isEqualTo("12ABC34501DE35");
        assertThat(Documentos.cnpjValido("12ABC34501DE35")).isTrue();
    }

    @Test
    @DisplayName("recusa CNPJ com dígito errado, letra no dígito verificador ou caracteres iguais")
    void recusaCnpjInvalido() {
        assertThat(Documentos.cnpjValido("11222333000182")).isFalse();
        assertThat(Documentos.cnpjValido("12ABC34501DE3A")).isFalse();
        assertThat(Documentos.cnpjValido("00000000000000")).isFalse();
        assertThat(Documentos.cnpjValido(null)).isFalse();
    }

}
