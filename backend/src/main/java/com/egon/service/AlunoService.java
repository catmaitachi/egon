package com.egon.service;

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.egon.dto.AlunoAtualizacaoRequest;
import com.egon.dto.AlunoCadastroRequest;
import com.egon.dto.AlunoResponse;
import com.egon.exception.ApiException;
import com.egon.model.Aluno;
import com.egon.repository.AlunoRepository;
import com.egon.repository.CursoRepository;
import com.egon.repository.InstituicaoRepository;
import com.egon.repository.UsuarioRepository;
import com.egon.validation.Documentos;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AlunoService {

    private final AlunoRepository alunos;
    private final InstituicaoRepository instituicoes;
    private final CursoRepository cursos;
    private final UsuarioRepository usuarios;
    private final PasswordEncoder senhas;

    @Transactional(readOnly = true)
    public List<AlunoResponse> listar() {
        return alunos.findAllByOrderByNomeAsc().stream().map(AlunoResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public AlunoResponse buscar(String cpf) {
        return alunos.findWithReferenciasByCpf(Documentos.normalizarCpf(cpf))
                .map(AlunoResponse::de)
                .orElseThrow(ApiException::alunoNaoEncontrado);
    }

    /** HU02: CPF, e-mail e login repetidos são recusados, cada um com seu aviso. */
    @Transactional
    public AlunoResponse cadastrar(AlunoCadastroRequest requisicao) {
        if (alunos.existsById(requisicao.cpf())) {
            throw ApiException.cpfEmUso();
        }
        if (alunos.existsByEmail(requisicao.email())) {
            throw ApiException.emailEmUso();
        }
        if (usuarios.existsByLogin(requisicao.login())) {
            throw ApiException.loginEmUso();
        }

        Aluno aluno = new Aluno();
        aluno.setCpf(requisicao.cpf());
        aluno.setNome(requisicao.nome());
        aluno.setEmail(requisicao.email());
        aluno.setRg(requisicao.rg());
        aluno.setEndereco(requisicao.endereco());
        aluno.setInstituicao(instituicoes.findById(requisicao.cnpjInstituicao())
                .orElseThrow(ApiException::instituicaoNaoEncontrada));
        aluno.setCurso(cursos.findById(requisicao.idCurso())
                .orElseThrow(ApiException::cursoNaoEncontrado));
        aluno.setLogin(requisicao.login());
        aluno.setSenha(senhas.encode(requisicao.senha()));
        return AlunoResponse.de(alunos.save(aluno));
    }

    @Transactional
    public AlunoResponse atualizar(String cpf, AlunoAtualizacaoRequest requisicao) {
        Aluno aluno = encontrar(cpf);

        if (alunos.existsByEmailAndCpfNot(requisicao.email(), aluno.getCpf())) {
            throw ApiException.emailEmUso();
        }
        if (!requisicao.login().equals(aluno.getLogin()) && usuarios.existsByLogin(requisicao.login())) {
            throw ApiException.loginEmUso();
        }

        aluno.setNome(requisicao.nome());
        aluno.setEmail(requisicao.email());
        aluno.setRg(requisicao.rg());
        aluno.setEndereco(requisicao.endereco());
        aluno.setInstituicao(instituicoes.findById(requisicao.cnpjInstituicao())
                .orElseThrow(ApiException::instituicaoNaoEncontrada));
        aluno.setCurso(cursos.findById(requisicao.idCurso())
                .orElseThrow(ApiException::cursoNaoEncontrado));
        aluno.setLogin(requisicao.login());
        if (requisicao.senha() != null) {
            aluno.setSenha(senhas.encode(requisicao.senha()));
        }
        return AlunoResponse.de(aluno);
    }

    @Transactional
    public void excluir(String cpf) {
        alunos.delete(encontrar(cpf));
    }

    private Aluno encontrar(String cpf) {
        return alunos.findById(Documentos.normalizarCpf(cpf))
                .orElseThrow(ApiException::alunoNaoEncontrado);
    }

}
