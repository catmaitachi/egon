package com.egon.repository;

import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

/**
 * Os perfis que fazem login vistos juntos.
 *
 * <p>Como não existe tabela USUARIO, o banco só garante login único dentro de cada tabela.
 * O login, porém, é um só para aluno, professor e empresa (HU01): um login de aluno não
 * pode existir também como login de empresa ou de professor.
 */
@Repository
@RequiredArgsConstructor
public class UsuarioRepository {

    private final AlunoRepository alunos;
    private final ProfessorRepository professores;
    private final EmpresaRepository empresas;

    public boolean existsByLogin(String login) {
        return alunos.existsByLogin(login) || professores.existsByLogin(login) || empresas.existsByLogin(login);
    }

}
