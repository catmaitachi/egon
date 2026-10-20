package com.egon.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.egon.dto.InstituicaoResponse;
import com.egon.repository.InstituicaoRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InstituicaoService {

    private final InstituicaoRepository instituicoes;

    /** A lista suspensa do cadastro de aluno (HU02). */
    @Transactional(readOnly = true)
    public List<InstituicaoResponse> listar() {
        return instituicoes.findAllByOrderByNomeAsc().stream().map(InstituicaoResponse::de).toList();
    }

}
