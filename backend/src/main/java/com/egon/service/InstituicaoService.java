package com.egon.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.egon.dto.InstituicaoAtualizacaoRequest;
import com.egon.dto.InstituicaoCadastroRequest;
import com.egon.dto.InstituicaoResponse;
import com.egon.exception.ApiException;
import com.egon.model.Instituicao;
import com.egon.repository.AlunoRepository;
import com.egon.repository.InstituicaoRepository;
import com.egon.repository.ProfessorRepository;

import lombok.RequiredArgsConstructor;

/** Instituições conveniadas: a lista do cadastro de aluno (HU02) e o CRUD do admin (HU04). */
@Service
@RequiredArgsConstructor
public class InstituicaoService {

    private final InstituicaoRepository instituicoes;
    private final AlunoRepository alunos;
    private final ProfessorRepository professores;

    @Transactional(readOnly = true)
    public List<InstituicaoResponse> listar() {
        return instituicoes.findAllByOrderByNomeAsc().stream().map(InstituicaoResponse::de).toList();
    }

    @Transactional
    public InstituicaoResponse cadastrar(InstituicaoCadastroRequest requisicao) {
        if (instituicoes.existsByCnpj(requisicao.cnpj())) {
            throw ApiException.cnpjDeInstituicaoEmUso();
        }
        return InstituicaoResponse.de(instituicoes.save(
                new Instituicao(requisicao.cnpj(), requisicao.nome(), requisicao.tipo())));
    }

    @Transactional
    public InstituicaoResponse atualizar(UUID id, InstituicaoAtualizacaoRequest requisicao) {
        Instituicao instituicao = encontrar(id);
        instituicao.setNome(requisicao.nome());
        instituicao.setTipo(requisicao.tipo());
        return InstituicaoResponse.de(instituicao);
    }

    /** Com alunos ou professores ela fica: apagá-la deixaria esses cadastros sem vínculo. */
    @Transactional
    public void excluir(UUID id) {
        Instituicao instituicao = encontrar(id);
        if (alunos.existsByInstituicaoId(id) || professores.existsByInstituicaoId(id)) {
            throw ApiException.instituicaoEmUso();
        }
        instituicoes.delete(instituicao);
    }

    private Instituicao encontrar(UUID id) {
        return instituicoes.findById(id).orElseThrow(ApiException::instituicaoInexistente);
    }

}
