package com.egon.service;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import lombok.extern.slf4j.Slf4j;

/**
 * Manda por SMTP os e-mails registrados, depois do commit. Sem {@code spring.mail.host}
 * configurado não há servidor para onde mandar: o e-mail fica só como notificação no painel.
 * Uma falha de envio não desfaz a operação que já foi gravada; fica no log.
 */
@Slf4j
@Component
class EnvioDeEmail {

    private final ObjectProvider<JavaMailSender> servidor;
    private final String remetente;

    EnvioDeEmail(ObjectProvider<JavaMailSender> servidor, @Value("${egon.email.remetente}") String remetente) {
        this.servidor = servidor;
        this.remetente = remetente;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    void enviar(EmailPendente email) {
        JavaMailSender smtp = servidor.getIfAvailable();
        if (smtp == null) {
            log.info("E-mail registrado no painel; sem SMTP configurado, não foi enviado. Para: {} | {}",
                    email.para(), email.assunto());
            return;
        }
        SimpleMailMessage mensagem = new SimpleMailMessage();
        mensagem.setFrom(remetente);
        mensagem.setTo(email.para());
        mensagem.setSubject(email.assunto());
        mensagem.setText(email.corpo());
        try {
            smtp.send(mensagem);
        } catch (MailException e) {
            log.warn("Falha ao enviar e-mail para {}: {}", email.para(), e.getMessage());
        }
    }

}
