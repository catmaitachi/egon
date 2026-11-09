package com.egon.notificacao;

import java.util.Map;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;

/**
 * Monta um e-mail a partir de um template HTML (em {@code resources/templates/email/}) e o manda por SMTP.
 *
 * <p>Sem {@code spring.mail.host} configurado não existe servidor para onde mandar: o e-mail não sai e
 * fica só registrado no log. O aviso ao vivo no app continua saindo do mesmo jeito.
 * Uma falha de envio também não desfaz nada: quem chama já está depois do commit.
 */
@Slf4j
@Component
public class EnvioDeEmail {

    private final ObjectProvider<JavaMailSender> servidor;
    private final ITemplateEngine templates;
    private final String remetente;

    EnvioDeEmail(ObjectProvider<JavaMailSender> servidor, ITemplateEngine templates,
            @Value("${egon.email.remetente}") String remetente) {
        this.servidor = servidor;
        this.templates = templates;
        this.remetente = remetente;
    }

    /** Devolve o HTML gerado, para quem quiser conferir (e para os testes). */
    public String gerar(String template, Map<String, Object> variaveis) {
        return templates.process(template, new Context(null, variaveis));
    }

    public void enviar(String para, String assunto, String template, Map<String, Object> variaveis) {
        String html = gerar(template, variaveis);
        JavaMailSender smtp = servidor.getIfAvailable();
        if (smtp == null) {
            log.info("Sem SMTP configurado (spring.mail.host); e-mail não enviado. Para: {} | {}", para, assunto);
            return;
        }
        try {
            MimeMessage mensagem = smtp.createMimeMessage();
            MimeMessageHelper ajudante = new MimeMessageHelper(mensagem, "UTF-8");
            ajudante.setFrom(remetente);
            ajudante.setTo(para);
            ajudante.setSubject(assunto);
            ajudante.setText(html, true);
            smtp.send(mensagem);
        } catch (MailException | MessagingException e) {
            log.warn("Falha ao enviar e-mail para {} ({}): {}", para, assunto, e.getMessage());
        }
    }

}
