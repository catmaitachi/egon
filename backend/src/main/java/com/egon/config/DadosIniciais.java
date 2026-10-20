package com.egon.config;

import java.util.List;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.egon.model.Curso;
import com.egon.model.Instituicao;
import com.egon.repository.CursoRepository;
import com.egon.repository.InstituicaoRepository;

import lombok.RequiredArgsConstructor;

/**
 * Instituições e cursos que o cadastro de aluno oferece nas listas.
 *
 * <p>Instituições conveniadas são pré-cadastradas, nunca criadas pela API (HU04). Estas são
 * fictícias, com CNPJs de dígitos válidos, até a lista real ser fornecida. Só entram com a
 * tabela vazia, então reiniciar a aplicação não duplica nada.
 */
@Component
@RequiredArgsConstructor
class DadosIniciais implements ApplicationRunner {

    private final InstituicaoRepository instituicoes;
    private final CursoRepository cursos;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (instituicoes.count() == 0) {
            instituicoes.saveAll(List.of(
                    new Instituicao("11222333000181", "Universidade Horizonte", "Universidade"),
                    new Instituicao("45218073000192", "Centro Universitário Vale Verde", "Centro Universitário"),
                    new Instituicao("28039156000103", "Faculdade de Tecnologia Aurora", "Faculdade")));
        }
        if (cursos.count() == 0) {
            cursos.saveAll(List.of(
                    new Curso("Administração"),
                    new Curso("Arquitetura e Urbanismo"),
                    new Curso("Ciência da Computação"),
                    new Curso("Direito"),
                    new Curso("Engenharia Civil"),
                    new Curso("Engenharia de Software"),
                    new Curso("Psicologia"),
                    new Curso("Sistemas de Informação")));
        }
    }

}
