package com.egon.resgate;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.IntStream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.egon.service.CodigoCupom;

class CodigoCupomTest {

    @Test
    @DisplayName("gera códigos no formato EGON-XXXX-XXXX, sem repetir em 10 mil")
    void gera() {
        Set<String> vistos = new HashSet<>();
        IntStream.range(0, 10_000).forEach(i -> {
            String codigo = CodigoCupom.novo();
            assertThat(codigo).matches(ResgateTest.FORMATO);
            vistos.add(codigo);
        });
        assertThat(vistos).hasSize(10_000);
    }

    @Test
    @DisplayName("normaliza o que a pessoa digita: caixa, espaços, sem hífen e letras parecidas com números")
    void normaliza() {
        assertThat(CodigoCupom.normalizar(" egon 7kq2 m9xd ")).isEqualTo("EGON-7KQ2-M9XD");
        assertThat(CodigoCupom.normalizar("EGON7KQ2M9XD")).isEqualTo("EGON-7KQ2-M9XD");
        assertThat(CodigoCupom.normalizar("egon-oil2-m9xd")).isEqualTo("EGON-0112-M9XD");
        assertThat(CodigoCupom.normalizar("7KQ2-M9XD")).isEqualTo("EGON-7KQ2-M9XD");
        assertThat(CodigoCupom.normalizar("lixo")).isNull();
    }

}
