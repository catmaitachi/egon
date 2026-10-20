package com.egon.service;

import java.time.Clock;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.egon.auth.Perfil;
import com.egon.auth.UsuarioLogado;
import com.egon.dto.TransacaoRequest;
import com.egon.dto.TransacaoResponse;
import com.egon.exception.ApiException;
import com.egon.model.Aluno;
import com.egon.model.Professor;
import com.egon.model.Transacao;
import com.egon.repository.TransacaoRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TransacaoService {

    private final ProfessorService professores;
    private final AlunoService alunos;
    private final NotificacaoService notificacoes;
    private final TransacaoRepository transacoes;
    private final Clock relogio;

    /**
     * HU06: debita o professor e credita o aluno na mesma transação, com as duas linhas travadas
     * (sempre professor antes de aluno), e avisa o aluno por e-mail (HU07).
     */
    @Transactional
    public TransacaoResponse enviar(UsuarioLogado usuario, TransacaoRequest requisicao) {
        Professor professor = professores.travar(usuario.exigir(Perfil.PROFESSOR))
                .orElseThrow(ApiException::naoAutenticado);
        Aluno aluno = alunos.travar(requisicao.cpfAluno())
                .orElseThrow(ApiException::alunoDestinatarioNaoEncontrado);
        if (!aluno.getInstituicao().getCnpj().equals(professor.getInstituicao().getCnpj())) {
            throw ApiException.alunoDeOutraInstituicao();
        }
        if (professor.getSaldo() < requisicao.valor()) {
            throw ApiException.saldoInsuficiente("valor", "Saldo insuficiente: você tem %s para distribuir.",
                    professor.getSaldo());
        }

        professor.setSaldo(professor.getSaldo() - requisicao.valor());
        aluno.setSaldo(aluno.getSaldo() + requisicao.valor());
        Transacao transacao = transacoes.save(new Transacao(professor, aluno, requisicao.valor(),
                requisicao.motivo(), LocalDateTime.now(relogio)));
        notificacoes.moedasRecebidas(transacao);

        return new TransacaoResponse(transacao.getId(), transacao.getData(), transacao.getValor(),
                transacao.getMotivo(), aluno.getNome(), professor.getSaldo());
    }

}
