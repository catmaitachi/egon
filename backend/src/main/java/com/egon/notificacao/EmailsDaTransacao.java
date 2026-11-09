package com.egon.notificacao;

import java.text.NumberFormat;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.egon.dto.TransacaoResponse;
import com.egon.model.Aluno;
import com.egon.model.Professor;
import com.egon.repository.AlunoRepository;
import com.egon.repository.ProfessorRepository;
import com.egon.service.TransacaoConfirmada;

import lombok.extern.slf4j.Slf4j;

/**
 * HU07: depois de cada envio de moedas confirmado, um e-mail para o aluno (moedas recebidas) e outro para o
 * professor (envio confirmado), cada um com o seu template.
 *
 * <p>Só depois do commit, como o aviso ao vivo: um envio desfeito não pode virar e-mail. E fora da thread da
 * requisição ({@code @Async}), para um servidor de e-mail lento não atrasar a resposta ao professor.
 */
@Slf4j
@Component
public class EmailsDaTransacao {

    static final String TEMPLATE_ALUNO = "email/moedas-recebidas";
    static final String TEMPLATE_PROFESSOR = "email/envio-confirmado";

    private static final Locale PT_BR = Locale.forLanguageTag("pt-BR");

    private final EnvioDeEmail email;
    private final AlunoRepository alunos;
    private final ProfessorRepository professores;
    private final DateTimeFormatter formatoData;
    private final String linkPainel;

    EmailsDaTransacao(EnvioDeEmail email, AlunoRepository alunos, ProfessorRepository professores,
            @Value("${egon.fuso}") String fuso, @Value("${egon.front.url}") String urlFront) {
        this.email = email;
        this.alunos = alunos;
        this.professores = professores;
        this.formatoData = DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm", PT_BR).withZone(ZoneId.of(fuso));
        this.linkPainel = urlFront.replaceAll("/+$", "") + "/painel";
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void aoConfirmar(TransacaoConfirmada evento) {
        TransacaoResponse transacao = evento.transacao();
        Aluno aluno = alunos.findById(transacao.aluno().id()).orElse(null);
        Professor professor = professores.findById(transacao.professor().id()).orElse(null);
        if (aluno == null || professor == null) {
            // Excluído entre o commit e aqui: não há para quem escrever.
            log.warn("Transação {} sem aluno ou professor para o e-mail", transacao.id());
            return;
        }

        Map<String, Object> comum = Map.of(
                "nomeAluno", aluno.getNome(),
                "nomeProfessor", professor.getNome(),
                "valor", egos(transacao.valor()),
                "data", formatoData.format(transacao.data()),
                "motivo", transacao.motivo(),
                "codigo", transacao.id().toString(),
                "linkPainel", linkPainel);

        email.enviar(aluno.getEmail(),
                "Você recebeu " + egos(transacao.valor()) + " de " + professor.getNome(),
                TEMPLATE_ALUNO, comSaldo(comum, evento.saldoAluno()));
        email.enviar(professor.getEmail(),
                "Envio confirmado: " + egos(transacao.valor()) + " para " + aluno.getNome(),
                TEMPLATE_PROFESSOR, comSaldo(comum, evento.saldoProfessor()));
    }

    private static Map<String, Object> comSaldo(Map<String, Object> comum, int saldo) {
        Map<String, Object> variaveis = new HashMap<>(comum);
        variaveis.put("saldo", egos(saldo));
        return variaveis;
    }

    /** "1.000 Egos", "1 Ego": como o app escreve as quantias. */
    static String egos(int valor) {
        return NumberFormat.getIntegerInstance(PT_BR).format(valor) + (valor == 1 ? " Ego" : " Egos");
    }

}
