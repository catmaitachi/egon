package com.egon.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.egon.dto.CursoRequest;
import com.egon.dto.CursoResponse;
import com.egon.exception.ApiException;
import com.egon.model.Curso;
import com.egon.repository.AlunoRepository;
import com.egon.repository.CursoRepository;

import lombok.RequiredArgsConstructor;

/** A lista de cursos do cadastro de aluno, mantida pelo admin. */
@Service
@RequiredArgsConstructor
public class CursoService {

    private final CursoRepository cursos;
    private final AlunoRepository alunos;

    @Transactional(readOnly = true)
    public List<CursoResponse> listar() {
        return cursos.findAllByOrderByNomeAsc().stream().map(CursoResponse::de).toList();
    }

    /** Nome repetido, mesmo com outra caixa, viraria duas opções iguais na lista. */
    @Transactional
    public CursoResponse cadastrar(CursoRequest requisicao) {
        if (cursos.existsByNomeIgnoreCase(requisicao.nome())) {
            throw ApiException.cursoRepetido();
        }
        return CursoResponse.de(cursos.save(new Curso(requisicao.nome())));
    }

    @Transactional
    public CursoResponse atualizar(Integer id, CursoRequest requisicao) {
        Curso curso = encontrar(id);
        if (cursos.existsByNomeIgnoreCaseAndIdNot(requisicao.nome(), id)) {
            throw ApiException.cursoRepetido();
        }
        curso.setNome(requisicao.nome());
        return CursoResponse.de(curso);
    }

    @Transactional
    public void excluir(Integer id) {
        Curso curso = encontrar(id);
        if (alunos.existsByCursoId(id)) {
            throw ApiException.cursoEmUso();
        }
        cursos.delete(curso);
    }

    private Curso encontrar(Integer id) {
        return cursos.findById(id).orElseThrow(ApiException::cursoInexistente);
    }

}
