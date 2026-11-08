package com.egon.notificacao;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.ConcurrentWebSocketSessionDecorator;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.egon.dto.Notificacao;
import com.egon.dto.NotificacaoCupom;
import com.egon.dto.NotificacaoTransferencia;
import com.egon.dto.TransacaoResponse;
import com.egon.security.Perfil;
import com.egon.service.CupomNotificado;
import com.egon.service.TransacaoConfirmada;
import com.egon.service.TransferenciaNotificada;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.json.JsonMapper;

/**
 * Avisos ao vivo de moedas recebidas e envio confirmado (HU07), dentro do app. O cliente só
 * escuta: conecta em {@code /ws/notificacoes} e recebe uma mensagem a cada envio
 * em que é o aluno ou o professor.
 *
 * <p>As sessões ficam só em memória. Quem não está conectado vê a transação no extrato.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificacaoHandler extends TextWebSocketHandler {

    /** Atributo da sessão com quem está conectado, posto pelo {@link PerfilInterceptor}. */
    static final String DESTINATARIO = "destinatario";

    /** Envio a um cliente lento espera até isso antes de derrubar a sessão dele. */
    private static final int LIMITE_ENVIO_MS = 10_000;
    private static final int LIMITE_BUFFER_BYTES = 512 * 1024;

    private final JsonMapper json;

    /** Destinatário → sessões abertas, por id. Cada aba do mesmo usuário é uma sessão e recebe tudo. */
    private final Map<String, Map<String, WebSocketSession>> sessoes = new ConcurrentHashMap<>();

    static String destinatario(Perfil perfil, UUID id) {
        return perfil + ":" + id;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession sessao) {
        // WebSocketSession não aceita dois envios ao mesmo tempo; o decorador enfileira.
        WebSocketSession segura = new ConcurrentWebSocketSessionDecorator(sessao, LIMITE_ENVIO_MS, LIMITE_BUFFER_BYTES);
        sessoes.compute(destinatario(sessao), (chave, abertas) -> {
            Map<String, WebSocketSession> mapa = abertas == null ? new ConcurrentHashMap<>() : abertas;
            mapa.put(sessao.getId(), segura);
            return mapa;
        });
    }

    @Override
    public void afterConnectionClosed(WebSocketSession sessao, CloseStatus status) {
        sessoes.computeIfPresent(destinatario(sessao), (chave, abertas) -> {
            abertas.remove(sessao.getId());
            return abertas.isEmpty() ? null : abertas;
        });
    }

    /** Só depois do commit: um envio desfeito não pode avisar de moedas que não existem. */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void aoConfirmar(TransacaoConfirmada evento) {
        TransacaoResponse transacao = evento.transacao();
        avisar(Perfil.ALUNO, transacao.aluno().id(), new Notificacao("MOEDAS_RECEBIDAS", transacao, evento.saldoAluno()));
        avisar(Perfil.PROFESSOR, transacao.professor().id(),
                new Notificacao("ENVIO_CONFIRMADO", transacao, evento.saldoProfessor()));
    }

    /** Repasse entre alunos: cada pessoa envolvida recebe o evento com o saldo dela. */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void aoMudarTransferencia(TransferenciaNotificada evento) {
        for (TransferenciaNotificada.Aviso aviso : evento.avisos()) {
            avisar(aviso.perfil(), aviso.id(),
                    new NotificacaoTransferencia(evento.evento(), evento.transferencia(), aviso.saldo()));
        }
    }

    /** Cupom emitido (aluno e empresa) ou usado (aluno). */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void aoMudarCupom(CupomNotificado evento) {
        for (TransferenciaNotificada.Aviso aviso : evento.avisos()) {
            avisar(aviso.perfil(), aviso.id(), new NotificacaoCupom(evento.evento(), evento.cupom(), aviso.saldo()));
        }
    }

    /** Manda a mensagem a todas as abas abertas dessa pessoa. Quem chama já está depois do commit. */
    public void avisar(Perfil perfil, UUID id, Object mensagem) {
        String destinatario = destinatario(perfil, id);
        Map<String, WebSocketSession> abertas = sessoes.get(destinatario);
        if (abertas == null) {
            return;
        }
        TextMessage texto = new TextMessage(json.writeValueAsString(mensagem));
        for (WebSocketSession sessao : abertas.values()) {
            try {
                sessao.sendMessage(texto);
            } catch (IOException | RuntimeException e) {
                // A transação já está gravada, e uma aba com problema não impede as outras.
                log.warn("Não foi possível notificar {} na sessão {}", destinatario, sessao.getId(), e);
            }
        }
    }

    private static String destinatario(WebSocketSession sessao) {
        return (String) sessao.getAttributes().get(DESTINATARIO);
    }

}
