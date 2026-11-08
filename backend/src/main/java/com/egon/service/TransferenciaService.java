package com.egon.service;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.egon.dto.RegrasRequest;
import com.egon.dto.RegrasResponse;
import com.egon.dto.TransferenciaRequest;
import com.egon.dto.TransferenciaResponse;
import com.egon.exception.ApiException;
import com.egon.model.Aluno;
import com.egon.model.Carteira;
import com.egon.model.Professor;
import com.egon.model.RegrasMoeda;
import com.egon.model.StatusTransferencia;
import com.egon.model.Transferencia;
import com.egon.repository.AlunoRepository;
import com.egon.repository.CarteiraRepository;
import com.egon.repository.ProfessorRepository;
import com.egon.repository.TransferenciaRepository;
import com.egon.security.ContaLogada;
import com.egon.security.Perfil;
import com.egon.service.TransferenciaNotificada.Aviso;

import lombok.RequiredArgsConstructor;

/**
 * Repasse de Egos entre alunos, sob as regras do professor de origem, com aprovação dele quando
 * ele exige.
 *
 * <p>Toda operação sobre os Egos de um professor trava a linha dele antes de qualquer carteira
 * (como o envio, HU06, e a cota, HU05): o mesmo Ego não é gasto duas vezes e não há ciclo de espera.
 */
@Service
@RequiredArgsConstructor
public class TransferenciaService {

    /** O mês do limite é o do calendário da instituição. */
    private static final ZoneId FUSO = ZoneId.of("America/Sao_Paulo");

    private final TransferenciaRepository transferencias;
    private final ProfessorRepository professores;
    private final AlunoRepository alunos;
    private final CarteiraRepository carteiras;
    private final Clock relogio;
    private final ApplicationEventPublisher eventos;

    @Transactional
    public TransferenciaResponse pedir(UUID idRemetente, TransferenciaRequest requisicao) {
        Professor professor = professores.findBloqueadoById(requisicao.professorId())
                .orElseThrow(() -> ApiException.saldoDaCarteiraInsuficiente(0));
        Aluno remetente = alunos.findById(idRemetente).orElseThrow(ApiException::alunoNaoEncontrado);
        Aluno destinatario = alunos.findById(requisicao.destinatarioId())
                .orElseThrow(() -> ApiException.destinatarioInvalido("Escolha um colega da lista."));
        if (destinatario.getId().equals(remetente.getId())) {
            throw ApiException.destinatarioInvalido("Escolha um colega, não você mesmo.");
        }
        if (!destinatario.getInstituicao().getId().equals(professor.getInstituicao().getId())) {
            throw ApiException.destinatarioInvalido(
                    "Os Egos deste professor só podem ir para alunos da instituição dele.");
        }
        Instant agora = relogio.instant().truncatedTo(ChronoUnit.MICROS);
        conferirRegras(professor.getRegras(), remetente, destinatario, professor, requisicao.valor(), agora);

        Carteira origem = carteiras.findBloqueadaByAlunoIdAndProfessorId(remetente.getId(), professor.getId())
                .orElseThrow(() -> ApiException.saldoDaCarteiraInsuficiente(0));
        if (origem.getSaldo() < requisicao.valor()) {
            throw ApiException.saldoDaCarteiraInsuficiente(origem.getSaldo());
        }
        origem.debitar(requisicao.valor());

        boolean direto = !professor.getRegras().isExigeAprovacao();
        Transferencia transferencia = transferencias.save(new Transferencia(agora, requisicao.valor(),
                requisicao.motivo(), remetente, destinatario, professor,
                direto ? StatusTransferencia.APROVADA : StatusTransferencia.PENDENTE));
        if (direto) {
            creditar(destinatario, professor, requisicao.valor());
            avisar("APROVADA", transferencia, aluno(remetente), aluno(destinatario));
        } else {
            avisar("SOLICITADA", transferencia, professor(professor), aluno(remetente));
        }
        return TransferenciaResponse.de(transferencia);
    }

    /** Aprovar e recusar são do professor de origem; cancelar, do remetente. Só pendente pode ser decidida. */
    @Transactional
    public TransferenciaResponse decidir(UUID id, StatusTransferencia decisao, ContaLogada quem) {
        UUID idProfessor = transferencias.findProfessorIdById(id).orElseThrow(ApiException::transferenciaNaoEncontrada);
        Professor professor = professores.findBloqueadoById(idProfessor).orElseThrow();
        Transferencia transferencia = transferencias.findBloqueadaById(id).orElseThrow();

        boolean podeDecidir = decisao == StatusTransferencia.CANCELADA
                ? quem.getPerfil() == Perfil.ALUNO && quem.getId().equals(transferencia.getRemetente().getId())
                : quem.getPerfil() == Perfil.PROFESSOR && quem.getId().equals(idProfessor);
        if (!podeDecidir) {
            throw new AccessDeniedException("Só o professor de origem decide; só quem pediu cancela.");
        }
        if (transferencia.getStatus() != StatusTransferencia.PENDENTE) {
            throw ApiException.transferenciaDecidida();
        }

        transferencia.decidir(decisao, relogio.instant().truncatedTo(ChronoUnit.MICROS));
        Aluno remetente = transferencia.getRemetente();
        switch (decisao) {
            case APROVADA -> {
                creditar(transferencia.getDestinatario(), professor, transferencia.getValor());
                avisar("APROVADA", transferencia, aluno(remetente), aluno(transferencia.getDestinatario()));
            }
            case RECUSADA -> {
                creditar(remetente, professor, transferencia.getValor());
                avisar("RECUSADA", transferencia, aluno(remetente));
            }
            case CANCELADA -> {
                creditar(remetente, professor, transferencia.getValor());
                avisar("CANCELADA", transferencia, professor(professor));
            }
            case PENDENTE -> throw new IllegalArgumentException("Decisão inválida.");
        }
        return TransferenciaResponse.de(transferencia);
    }

    /** Pendentes primeiro (o que espera o professor), depois as decididas, das mais recentes. */
    @Transactional(readOnly = true)
    public List<TransferenciaResponse> doProfessor(UUID professor) {
        return transferencias.findByProfessorIdOrderByDataDesc(professor).stream()
                .sorted(Comparator.comparing((Transferencia t) -> t.getStatus() != StatusTransferencia.PENDENTE))
                .map(TransferenciaResponse::de)
                .toList();
    }

    @Transactional(readOnly = true)
    public RegrasResponse regras(UUID professor) {
        return RegrasResponse.de(professores.findById(professor).orElseThrow(ApiException::professorNaoEncontrado).getRegras());
    }

    @Transactional
    public RegrasResponse definirRegras(UUID idProfessor, RegrasRequest requisicao) {
        Professor professor = professores.findBloqueadoById(idProfessor).orElseThrow(ApiException::professorNaoEncontrado);
        RegrasMoeda regras = professor.getRegras();
        regras.setPermiteRepasse(requisicao.permiteRepasse());
        regras.setExigeAprovacao(requisicao.exigeAprovacao());
        regras.setValorMaximo(requisicao.valorMaximo());
        regras.setLimiteMensal(requisicao.limiteMensal());
        regras.setMesmoCurso(requisicao.mesmoCurso());
        return RegrasResponse.de(regras);
    }

    private void conferirRegras(RegrasMoeda regras, Aluno remetente, Aluno destinatario, Professor professor,
            int valor, Instant agora) {
        if (!regras.isPermiteRepasse()) {
            throw ApiException.repasseDesativado();
        }
        if (regras.getValorMaximo() != null && valor > regras.getValorMaximo()) {
            throw ApiException.acimaDoMaximo(regras.getValorMaximo());
        }
        if (regras.isMesmoCurso() && !destinatario.getCurso().getId().equals(remetente.getCurso().getId())) {
            throw ApiException.cursoDiferente();
        }
        if (regras.getLimiteMensal() != null) {
            Instant inicioDoMes = agora.atZone(FUSO).withDayOfMonth(1).truncatedTo(ChronoUnit.DAYS).toInstant();
            int usado = transferencias.somaNoMes(remetente.getId(), professor.getId(), inicioDoMes);
            if (usado + valor > regras.getLimiteMensal()) {
                throw ApiException.limiteMensal(regras.getLimiteMensal(), usado);
            }
        }
    }

    private void creditar(Aluno aluno, Professor professor, int valor) {
        carteiras.findBloqueadaByAlunoIdAndProfessorId(aluno.getId(), professor.getId())
                .orElseGet(() -> carteiras.save(new Carteira(aluno, professor)))
                .creditar(valor);
    }

    private Aviso aluno(Aluno aluno) {
        return new Aviso(Perfil.ALUNO, aluno.getId(), carteiras.somaPorAluno(aluno.getId()));
    }

    private static Aviso professor(Professor professor) {
        return new Aviso(Perfil.PROFESSOR, professor.getId(), professor.getSaldo());
    }

    private void avisar(String evento, Transferencia transferencia, Aviso... avisos) {
        eventos.publishEvent(new TransferenciaNotificada(evento, TransferenciaResponse.de(transferencia),
                new ArrayList<>(List.of(avisos))));
    }

}
