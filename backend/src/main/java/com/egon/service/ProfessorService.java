package com.egon.service;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.egon.dto.ColegaResponse;
import com.egon.dto.ExtratoResponse;
import com.egon.dto.ProfessorAtualizacaoRequest;
import com.egon.dto.ProfessorCadastroRequest;
import com.egon.dto.Movimento;
import com.egon.dto.ProfessorResponse;
import com.egon.exception.ApiException;
import com.egon.model.Professor;
import com.egon.repository.AlunoRepository;
import com.egon.repository.CotaSemestralRepository;
import com.egon.repository.InstituicaoRepository;
import com.egon.repository.ProfessorRepository;
import com.egon.repository.TransacaoRepository;
import com.egon.repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProfessorService {

    private final ProfessorRepository professores;
    private final TransacaoRepository transacoes;
    private final CotaSemestralRepository cotas;
    private final CotaSemestralService cotasDoSemestre;
    private final InstituicaoRepository instituicoes;
    private final UsuarioRepository usuarios;
    private final PasswordEncoder senhas;
    private final AlunoRepository alunos;

    @Transactional(readOnly = true)
    public List<ProfessorResponse> listar() {
        return professores.findAllByOrderByNomeAsc().stream().map(ProfessorResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public ProfessorResponse buscar(UUID id) {
        return professores.findWithReferenciasById(id)
                .map(ProfessorResponse::de)
                .orElseThrow(ApiException::professorNaoEncontrado);
    }

    /** HU04: o admin cadastra o professor, que já recebe a cota do semestre (HU05). */
    @Transactional
    public ProfessorResponse cadastrar(ProfessorCadastroRequest requisicao) {
        if (professores.existsByCpf(requisicao.cpf())) {
            throw ApiException.cpfDeProfessorEmUso();
        }
        if (professores.existsByEmail(requisicao.email())) {
            throw ApiException.emailEmUso();
        }
        if (usuarios.existsByLogin(requisicao.login())) {
            throw ApiException.loginEmUso();
        }
        Professor professor = new Professor();
        professor.setCpf(requisicao.cpf());
        professor.setNome(requisicao.nome());
        professor.setEmail(requisicao.email());
        professor.setDepartamento(requisicao.departamento());
        professor.setInstituicao(instituicoes.findById(requisicao.idInstituicao())
                .orElseThrow(ApiException::instituicaoNaoEncontrada));
        professor.setLogin(requisicao.login());
        professor.setSenha(senhas.encode(requisicao.senha()));
        professores.save(professor);
        cotasDoSemestre.creditar(professor);
        return ProfessorResponse.de(professor);
    }

    /** Com trava: o saldo também é gravado no UPDATE, e um envio no meio não pode se perder. */
    @Transactional
    public ProfessorResponse atualizar(UUID id, ProfessorAtualizacaoRequest requisicao) {
        Professor professor = professores.findBloqueadoById(id).orElseThrow(ApiException::professorNaoEncontrado);
        if (professores.existsByEmailAndIdNot(requisicao.email(), id)) {
            throw ApiException.emailEmUso();
        }
        if (!requisicao.login().equals(professor.getLogin()) && usuarios.existsByLogin(requisicao.login())) {
            throw ApiException.loginEmUso();
        }
        professor.setNome(requisicao.nome());
        professor.setEmail(requisicao.email());
        professor.setDepartamento(requisicao.departamento());
        professor.setInstituicao(instituicoes.findById(requisicao.idInstituicao())
                .orElseThrow(ApiException::instituicaoNaoEncontrada));
        professor.setLogin(requisicao.login());
        if (requisicao.senha() != null) {
            professor.setSenha(senhas.encode(requisicao.senha()));
        }
        return ProfessorResponse.de(professor);
    }

    /** Quem já enviou fica: os Egos dele estão nas carteiras e no histórico dos alunos. */
    @Transactional
    public void excluir(UUID id) {
        Professor professor = professores.findBloqueadoById(id).orElseThrow(ApiException::professorNaoEncontrado);
        if (transacoes.existsByProfessorId(id)) {
            throw ApiException.professorComTransacoes();
        }
        cotas.deleteByProfessorId(id);
        professores.delete(professor);
    }

    /** HU06: os alunos da instituição do professor, para escolher a quem enviar. */
    @Transactional(readOnly = true)
    public List<ColegaResponse> alunos(UUID id) {
        Professor professor = professores.findById(id).orElseThrow(ApiException::professorNaoEncontrado);
        return alunos.findByInstituicaoIdOrderByNomeAsc(professor.getInstituicao().getId()).stream()
                .map(ColegaResponse::de).toList();
    }

    /** HU12: os envios e as cotas recebidas (a HU05 manda a cota constar no histórico). */
    @Transactional(readOnly = true)
    public ExtratoResponse extrato(UUID id) {
        Professor professor = professores.findById(id)
                .orElseThrow(ApiException::professorNaoEncontrado);
        List<Movimento> movimentos = Stream.concat(
                        transacoes.findByProfessorIdOrderByDataDesc(professor.getId()).stream().map(Movimento::envio),
                        cotas.findByProfessorIdOrderByDataDesc(professor.getId()).stream().map(Movimento::cota))
                .sorted(Comparator.comparing(Movimento::data).reversed())
                .toList();
        return new ExtratoResponse(professor.getSaldo(), List.of(), movimentos);
    }

}
