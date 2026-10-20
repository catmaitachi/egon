package com.egon.config;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.egon.model.Curso;
import com.egon.model.Instituicao;
import com.egon.model.Professor;
import com.egon.repository.CursoRepository;
import com.egon.repository.InstituicaoRepository;
import com.egon.repository.ProfessorRepository;
import com.egon.repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;

/**
 * Dados que não são cadastrados pela API: instituições e cursos das listas do cadastro de aluno
 * e os professores que as instituições fornecem (HU04).
 *
 * <p>Todos fictícios, com CPFs e CNPJs de dígitos válidos, até as listas reais serem
 * fornecidas. Cada grupo só entra com a tabela vazia, então reiniciar a aplicação não duplica nada.
 *
 * <p>Roda antes de o servidor web aceitar conexões, e não num {@code ApplicationRunner}, que
 * roda depois: na subida, o formulário que abrisse nesse intervalo receberia listas vazias e o
 * front-end as guardaria para a sessão inteira.
 */
@Component
@RequiredArgsConstructor
class DadosIniciais implements SmartInitializingSingleton {

    /** Senha inicial dos professores pré-cadastrados (HU04), para o ambiente de desenvolvimento. */
    static final String SENHA_INICIAL_PROFESSOR = "professor123";

    private final InstituicaoRepository instituicoes;
    private final CursoRepository cursos;
    private final ProfessorRepository professores;
    private final UsuarioRepository usuarios;
    private final PasswordEncoder senhas;

    @Override
    @Transactional
    public void afterSingletonsInstantiated() {
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
        if (professores.count() == 0) {
            preCadastrarProfessores();
        }
    }

    private void preCadastrarProfessores() {
        Map<String, Curso> porNome = cursos.findAll().stream()
                .collect(Collectors.toMap(Curso::getNome, Function.identity()));
        String senha = senhas.encode(SENHA_INICIAL_PROFESSOR);
        record Dados(String cpf, String nome, String departamento, String cnpjInstituicao, String login, List<String> cursos) {
        }
        List<Dados> lista = List.of(
                new Dados("18473625161", "Helena Duarte", "Departamento de Computação", "11222333000181",
                        "helena.duarte", List.of("Ciência da Computação", "Engenharia de Software", "Sistemas de Informação")),
                new Dados("29384756164", "Ricardo Moura", "Departamento de Engenharia", "11222333000181",
                        "ricardo.moura", List.of("Engenharia Civil", "Arquitetura e Urbanismo")),
                new Dados("37561029470", "Marcos Tavares", "Departamento de Ciências Sociais Aplicadas", "45218073000192",
                        "marcos.tavares", List.of("Administração", "Direito", "Psicologia")),
                new Dados("46820517335", "Lúcia Fernandes", "Departamento de Tecnologia", "28039156000103",
                        "lucia.fernandes", List.of("Sistemas de Informação", "Engenharia de Software")));
        for (Dados dados : lista) {
            // Um login já usado por aluno ou empresa não pode ser repetido (HU01).
            if (usuarios.existsByLogin(dados.login())) {
                continue;
            }
            Professor professor = new Professor();
            professor.setCpf(dados.cpf());
            professor.setNome(dados.nome());
            professor.setDepartamento(dados.departamento());
            professor.setInstituicao(instituicoes.getReferenceById(dados.cnpjInstituicao()));
            professor.setLogin(dados.login());
            professor.setSenha(senha);
            dados.cursos().forEach(nome -> professor.getCursos().add(porNome.get(nome)));
            professores.save(professor);
        }
    }

}
