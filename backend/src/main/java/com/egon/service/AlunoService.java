package com.egon.service;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.egon.dto.AlunoAtualizacaoRequest;
import com.egon.dto.AlunoCadastroRequest;
import com.egon.dto.AlunoResponse;
import com.egon.dto.CarteiraResponse;
import com.egon.dto.ColegaResponse;
import com.egon.dto.ExtratoResponse;
import com.egon.dto.Movimento;
import com.egon.exception.ApiException;
import com.egon.model.Aluno;
import com.egon.model.StatusTransferencia;
import com.egon.repository.AlunoRepository;
import com.egon.repository.CarteiraRepository;
import com.egon.repository.CupomRepository;
import com.egon.repository.CursoRepository;
import com.egon.repository.InstituicaoRepository;
import com.egon.repository.TransacaoRepository;
import com.egon.repository.TransferenciaRepository;
import com.egon.repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AlunoService {

    private final AlunoRepository alunos;
    private final InstituicaoRepository instituicoes;
    private final CursoRepository cursos;
    private final TransacaoRepository transacoes;
    private final CarteiraRepository carteiras;
    private final TransferenciaRepository transferencias;
    private final CupomRepository cupons;
    private final UsuarioRepository usuarios;
    private final PasswordEncoder senhas;

    @Transactional(readOnly = true)
    public List<AlunoResponse> listar() {
        return alunos.findAllByOrderByNomeAsc().stream().map(this::resposta).toList();
    }

    @Transactional(readOnly = true)
    public AlunoResponse buscar(UUID id) {
        return alunos.findWithReferenciasById(id)
                .map(this::resposta)
                .orElseThrow(ApiException::alunoNaoEncontrado);
    }

    /** HU02: CPF, e-mail e login repetidos são recusados, cada um com seu aviso. */
    @Transactional
    public AlunoResponse cadastrar(AlunoCadastroRequest requisicao) {
        if (alunos.existsByCpf(requisicao.cpf())) {
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
        aluno.setInstituicao(instituicoes.findById(requisicao.idInstituicao())
                .orElseThrow(ApiException::instituicaoNaoEncontrada));
        aluno.setCurso(cursos.findById(requisicao.idCurso())
                .orElseThrow(ApiException::cursoNaoEncontrado));
        aluno.setLogin(requisicao.login());
        aluno.setSenha(senhas.encode(requisicao.senha()));
        return AlunoResponse.de(alunos.save(aluno), 0);
    }

    /** O CPF não muda: identifica a pessoa. */
    @Transactional
    public AlunoResponse atualizar(UUID id, AlunoAtualizacaoRequest requisicao) {
        Aluno aluno = encontrarBloqueado(id);

        if (alunos.existsByEmailAndIdNot(requisicao.email(), aluno.getId())) {
            throw ApiException.emailEmUso();
        }
        if (!requisicao.login().equals(aluno.getLogin()) && usuarios.existsByLogin(requisicao.login())) {
            throw ApiException.loginEmUso();
        }

        aluno.setNome(requisicao.nome());
        aluno.setEmail(requisicao.email());
        aluno.setRg(requisicao.rg());
        aluno.setEndereco(requisicao.endereco());
        aluno.setInstituicao(instituicoes.findById(requisicao.idInstituicao())
                .orElseThrow(ApiException::instituicaoNaoEncontrada));
        aluno.setCurso(cursos.findById(requisicao.idCurso())
                .orElseThrow(ApiException::cursoNaoEncontrado));
        aluno.setLogin(requisicao.login());
        if (requisicao.senha() != null) {
            aluno.setSenha(senhas.encode(requisicao.senha()));
        }
        return resposta(aluno);
    }

    /**
     * HU11: recebimentos de professores e repasses entre colegas, do mais recente ao mais antigo.
     * Quem repassou vê o repasse em qualquer status; quem recebe, só o aprovado. As trocas por
     * vantagens (HU09) aparecem como saída, com o código do cupom.
     */
    @Transactional(readOnly = true)
    public ExtratoResponse extrato(UUID id) {
        Aluno aluno = encontrar(id);
        Stream<Movimento> repasses = transferencias.findByRemetenteIdOrDestinatarioIdOrderByDataDesc(id, id).stream()
                .filter(t -> t.getRemetente().getId().equals(id) || t.getStatus() == StatusTransferencia.APROVADA)
                .map(t -> t.getRemetente().getId().equals(id) ? Movimento.repasseEnviado(t) : Movimento.repasseRecebido(t));
        Stream<Movimento> resgates = cupons.findByTrocaAlunoIdOrderByTrocaDataDesc(id).stream().map(Movimento::resgate);
        return new ExtratoResponse(carteiras.somaPorAluno(aluno.getId()),
                carteiras.findByAlunoIdOrderByProfessorNomeAsc(aluno.getId()).stream().map(CarteiraResponse::de).toList(),
                Stream.of(transacoes.findByAlunoIdOrderByDataDesc(id).stream().map(Movimento::recebimento), repasses, resgates)
                        .flatMap(s -> s)
                        .sorted(Comparator.comparing(Movimento::data).reversed())
                        .toList());
    }

    /** Colegas da mesma instituição, para escolher a quem repassar. */
    @Transactional(readOnly = true)
    public List<ColegaResponse> colegas(UUID id) {
        Aluno aluno = encontrar(id);
        return alunos.findByInstituicaoIdOrderByNomeAsc(aluno.getInstituicao().getId()).stream()
                .filter(colega -> !colega.getId().equals(id))
                .map(ColegaResponse::de)
                .toList();
    }

    /**
     * Quem já recebeu moedas fica: apagá-lo apagaria também o histórico de quem enviou. A trava
     * impede que um envio aconteça entre a checagem e o DELETE.
     */
    @Transactional
    public void excluir(UUID id) {
        Aluno aluno = encontrarBloqueado(id);
        if (transacoes.existsByAlunoId(id) || transferencias.existsByRemetenteIdOrDestinatarioId(id, id)) {
            throw ApiException.alunoComTransacoes();
        }
        alunos.delete(aluno);
    }

    private AlunoResponse resposta(Aluno aluno) {
        return AlunoResponse.de(aluno, carteiras.somaPorAluno(aluno.getId()));
    }

    private Aluno encontrar(UUID id) {
        return alunos.findById(id).orElseThrow(ApiException::alunoNaoEncontrado);
    }

    /** Sem ciclo de espera com o envio: ele trava o professor antes do aluno, e aqui só o aluno é travado. */
    private Aluno encontrarBloqueado(UUID id) {
        return alunos.findBloqueadoById(id).orElseThrow(ApiException::alunoNaoEncontrado);
    }

}
