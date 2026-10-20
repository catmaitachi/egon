package com.egon.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.egon.dto.CursoResponse;
import com.egon.repository.CursoRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CursoService {

    private final CursoRepository cursos;

    @Transactional(readOnly = true)
    public List<CursoResponse> listar() {
        return cursos.findAllByOrderByNomeAsc().stream().map(CursoResponse::de).toList();
    }

}
