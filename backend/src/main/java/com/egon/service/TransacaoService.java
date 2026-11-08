package com.egon.service;

import java.time.Clock;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.egon.dto.TransacaoRequest;
import com.egon.dto.TransacaoResponse;
import com.egon.exception.ApiException;
import com.egon.model.Aluno;
import com.egon.model.Carteira;
import com.egon.model.Professor;
import com.egon.model.Transacao;
import com.egon.repository.AlunoRepository;
import com.egon.repository.CarteiraRepository;
import com.egon.repository.ProfessorRepository;
import com.egon.repository.TransacaoRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TransacaoService {

    private final ProfessorRepository professores;
    private final AlunoRepository alunos;
    private final TransacaoRepository transacoes;
    private final CarteiraRepository carteiras;
    private final Clock relogio;
    private final ApplicationEventPublisher eventos;

    /**
     * HU06: o professor envia moedas a um aluno da mesma instituição, com um motivo.
     *
     * <p>Os dois saldos são lidos com trava, sempre o professor antes do aluno: dois envios
     * simultâneos nunca gastam o mesmo saldo nem perdem um crédito, e a ordem fixa impede que
     * duas transações fiquem esperando uma pela outra.
     */
    @Transactional
    public TransacaoResponse enviar(UUID idProfessor, TransacaoRequest requisicao) {
        Professor professor = professores.findBloqueadoById(idProfessor)
                .orElseThrow(ApiException::professorNaoEncontrado);
        Aluno aluno = alunos.findBloqueadoById(requisicao.idAluno())
                .orElseThrow(ApiException::alunoNaoEncontradoNoEnvio);
        if (!aluno.getInstituicao().getId().equals(professor.getInstituicao().getId())) {
            throw ApiException.alunoDeOutraInstituicao();
        }
        int valor = requisicao.valor();
        if (professor.getSaldo() < valor) {
            throw ApiException.saldoInsuficiente(professor.getSaldo());
        }

        professor.setSaldo(professor.getSaldo() - valor);
        Carteira carteira = carteiras.findBloqueadaByAlunoIdAndProfessorId(aluno.getId(), professor.getId())
                .orElseGet(() -> carteiras.save(new Carteira(aluno, professor)));
        carteira.creditar(valor);
        // Na precisão da coluna (microssegundos): a resposta mostra a mesma data que o extrato vai mostrar.
        Transacao transacao = transacoes.save(new Transacao(
                relogio.instant().truncatedTo(ChronoUnit.MICROS), valor, requisicao.motivo(), professor, aluno));
        TransacaoResponse resposta = TransacaoResponse.de(transacao);
        eventos.publishEvent(new TransacaoConfirmada(resposta, professor.getSaldo(), carteiras.somaPorAluno(aluno.getId())));
        return resposta;
    }

}
