package com.egon.notificacao;

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
import org.springframework.web.util.UriComponentsBuilder;

import com.egon.dto.CupomResponse;
import com.egon.model.Aluno;
import com.egon.model.Empresa;
import com.egon.model.Vantagem;
import com.egon.repository.AlunoRepository;
import com.egon.repository.EmpresaRepository;
import com.egon.repository.VantagemRepository;
import com.egon.security.Perfil;
import com.egon.service.CupomNotificado;

import lombok.extern.slf4j.Slf4j;

/**
 * HU10: quando um resgate emite o cupom, um e-mail ao aluno (o cupom, para a troca presencial) e outro à
 * empresa parceira (para conferir a troca), os dois com o mesmo código gerado pelo sistema.
 *
 * <p>Como os e-mails de moedas: só depois do commit e fora da thread da requisição. O uso do cupom
 * ({@code USADO}) continua só com o aviso no app.
 */
@Slf4j
@Component
public class EmailsDoCupom {

    static final String TEMPLATE_ALUNO = "email/cupom-aluno";
    static final String TEMPLATE_EMPRESA = "email/cupom-empresa";

    private static final Locale PT_BR = Locale.forLanguageTag("pt-BR");

    private final EnvioDeEmail email;
    private final AlunoRepository alunos;
    private final EmpresaRepository empresas;
    private final VantagemRepository vantagens;
    private final DateTimeFormatter formatoData;
    private final String urlFront;

    EmailsDoCupom(EnvioDeEmail email, AlunoRepository alunos, EmpresaRepository empresas, VantagemRepository vantagens,
            @Value("${egon.fuso}") String fuso, @Value("${egon.front.url}") String urlFront) {
        this.email = email;
        this.alunos = alunos;
        this.empresas = empresas;
        this.vantagens = vantagens;
        this.formatoData = DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm", PT_BR).withZone(ZoneId.of(fuso));
        this.urlFront = urlFront.replaceAll("/+$", "");
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void aoNotificar(CupomNotificado evento) {
        if (!"EMITIDO".equals(evento.evento())) {
            return;
        }
        CupomResponse cupom = evento.cupom();
        Aluno aluno = alunos.findWithReferenciasById(cupom.aluno().id()).orElse(null);
        Empresa empresa = empresas.findById(cupom.empresa().id()).orElse(null);
        Vantagem vantagem = vantagens.findById(cupom.vantagem().id()).orElse(null);
        if (aluno == null || empresa == null || vantagem == null) {
            // Excluído entre o commit e aqui: não há para quem escrever.
            log.warn("Cupom {} sem aluno, empresa ou vantagem para o e-mail", cupom.codigo());
            return;
        }
        int saldoAluno = evento.avisos().stream()
                .filter(a -> a.perfil() == Perfil.ALUNO)
                .mapToInt(a -> a.saldo())
                .findFirst().orElse(0);

        Map<String, Object> comum = new HashMap<>();
        comum.put("codigo", cupom.codigo());
        comum.put("nomeVantagem", vantagem.getNome());
        comum.put("descricaoVantagem", vantagem.getDescricao());
        comum.put("valor", EmailsDaTransacao.egos(cupom.valor()));
        comum.put("data", formatoData.format(cupom.data()));
        comum.put("nomeEmpresa", empresa.getNome());
        comum.put("nomeAluno", aluno.getNome());

        Map<String, Object> doAluno = new HashMap<>(comum);
        doAluno.put("saldo", EmailsDaTransacao.egos(saldoAluno));
        doAluno.put("linkPainel", urlFront + "/painel");
        email.enviar(aluno.getEmail(), "Seu cupom " + cupom.codigo() + ": " + vantagem.getNome(), TEMPLATE_ALUNO, doAluno);

        Map<String, Object> daEmpresa = new HashMap<>(comum);
        daEmpresa.put("emailAluno", aluno.getEmail());
        daEmpresa.put("cursoAluno", aluno.getCurso().getNome());
        daEmpresa.put("instituicaoAluno", aluno.getInstituicao().getNome());
        // O mesmo link do QR code do app: a empresa logada cai na conferência com o código preenchido.
        daEmpresa.put("linkConferir", UriComponentsBuilder.fromUriString(urlFront + "/painel")
                .queryParam("cupom", cupom.codigo()).build().toUriString());
        email.enviar(empresa.getEmail(), "Novo resgate: " + vantagem.getNome() + " (cupom " + cupom.codigo() + ")",
                TEMPLATE_EMPRESA, daEmpresa);
    }

}
