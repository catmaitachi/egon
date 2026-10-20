package com.egon.service;

import java.util.List;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.egon.auth.Perfil;
import com.egon.auth.UsuarioLogado;
import com.egon.dto.AlunoAtualizacaoRequest;
import com.egon.dto.AlunoCadastroRequest;
import com.egon.dto.AlunoResponse;
import com.egon.dto.ExtratoResponse;
import com.egon.dto.LancamentoResponse;
import com.egon.exception.ApiException;
import com.egon.model.Aluno;
import com.egon.repository.AlunoRepository;
import com.egon.repository.CursoRepository;
import com.egon.repository.InstituicaoRepository;
import com.egon.repository.TransacaoRepository;
import com.egon.repository.TrocaRepository;
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
    private final TransacaoRepository transacoes;
    private final TrocaRepository trocas;
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

    /** Aluno com moedas recebidas ou trocas não sai: o extrato dos professores e os cupons dependem dele. */
    @Transactional
    public void excluir(String cpf) {
        Aluno aluno = encontrar(cpf);
        if (transacoes.existsByAlunoCpf(aluno.getCpf()) || trocas.existsByAlunoCpf(aluno.getCpf())) {
            throw ApiException.alunoComHistorico();
        }
        alunos.delete(aluno);
    }

    /** HU11: saldo, moedas recebidas (professor e motivo) e trocas (vantagem, empresa e cupom). */
    @Transactional(readOnly = true)
    public ExtratoResponse extrato(UsuarioLogado usuario) {
        Aluno aluno = alunos.findById(usuario.exigir(Perfil.ALUNO)).orElseThrow(ApiException::naoAutenticado);
        List<LancamentoResponse> entradas = transacoes.findByAlunoCpf(aluno.getCpf()).stream()
                .map(t -> LancamentoResponse.transacao("ENTRADA", t.getData(), t.getValor(),
                        t.getProfessor().getNome(), t.getMotivo()))
                .toList();
        List<LancamentoResponse> saidas = trocas.doAluno(aluno.getCpf()).stream()
                .map(t -> LancamentoResponse.troca(t.getData(), t.getValor(), t.getVantagem().getNome(),
                        t.getVantagem().getEmpresa().getNome(),
                        t.getCupom() == null ? null : t.getCupom().getCodigo()))
                .toList();
        return ExtratoResponse.de(aluno.getSaldo(), entradas, saidas);
    }

    /** Aluno com a linha travada, para quem vai mexer no saldo dentro da transação corrente. */
    @Transactional(propagation = Propagation.MANDATORY)
    public Optional<Aluno> travar(String cpf) {
        return alunos.travar(cpf);
    }

    private Aluno encontrar(String cpf) {
        return alunos.findById(Documentos.normalizarCpf(cpf))
                .orElseThrow(ApiException::alunoNaoEncontrado);
    }

}
