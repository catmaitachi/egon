package com.egon.notificacao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.after;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.egon.Dados;
import com.egon.dto.AlunoCadastroRequest;
import com.egon.dto.TransacaoRequest;
import com.egon.exception.ApiException;
import com.egon.model.Professor;
import com.egon.repository.AlunoRepository;
import com.egon.repository.CarteiraRepository;
import com.egon.repository.CursoRepository;
import com.egon.repository.InstituicaoRepository;
import com.egon.repository.ProfessorRepository;
import com.egon.repository.TransacaoRepository;
import com.egon.service.AlunoService;
import com.egon.service.TransacaoService;

import jakarta.mail.Message.RecipientType;
import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

/**
 * HU07 por e-mail: cada envio de moedas confirmado manda um e-mail ao aluno e outro ao professor, cada um com
 * o seu template. O servidor SMTP é um dublê que guarda as mensagens.
 *
 * <p>O e-mail só sai depois do commit, então nada aqui é {@code @Transactional}: o que o teste grava é apagado
 * no fim, como no teste do WebSocket.
 */
@SpringBootTest
class EmailsDaTransacaoTest {

    private static final String EMAIL_HELENA = "helena.duarte@horizonte.edu.br";
    private static final String EMAIL_ANA = "ana@email.com";

    @MockitoBean JavaMailSender smtp;
    @Autowired TransacaoService transacoes;
    @Autowired AlunoService alunoService;
    @Autowired AlunoRepository alunos;
    @Autowired ProfessorRepository professores;
    @Autowired TransacaoRepository transacaoRepository;
    @Autowired CarteiraRepository carteiras;
    @Autowired CursoRepository cursos;
    @Autowired InstituicaoRepository instituicoes;

    private UUID helena;
    private UUID ana;
    private int saldoHelena;

    @BeforeEach
    void preparar() {
        when(smtp.createMimeMessage()).thenAnswer(chamada -> new MimeMessage(Session.getInstance(new Properties())));
        helena = professores.findByCpf(Dados.HELENA).orElseThrow().getId();
        saldoHelena = professores.findById(helena).orElseThrow().getSaldo();
        ana = alunoService.cadastrar(new AlunoCadastroRequest("Ana Souza", EMAIL_ANA, Dados.ANA, "123",
                "Rua A, 1", instituicoes.findByCnpj(Dados.HORIZONTE).orElseThrow().getId(),
                cursos.findAllByOrderByNomeAsc().getFirst().getId(), "ana", "segredo123")).id();
    }

    @AfterEach
    void limpar() {
        transacaoRepository.deleteAll(transacaoRepository.findByAlunoIdOrderByDataDesc(ana));
        carteiras.deleteAll(carteiras.findByAlunoIdOrderByProfessorNomeAsc(ana));
        alunos.deleteById(ana);
        Professor professor = professores.findById(helena).orElseThrow();
        professor.setSaldo(saldoHelena);
        professores.save(professor);
    }

    /** Espera os dois e-mails (saem numa thread à parte) e os devolve pelo destinatário. */
    private Map<String, MimeMessage> doisEmails() throws Exception {
        ArgumentCaptor<MimeMessage> enviados = ArgumentCaptor.forClass(MimeMessage.class);
        verify(smtp, timeout(5000).times(2)).send(enviados.capture());
        List<MimeMessage> mensagens = enviados.getAllValues();
        return mensagens.stream().collect(Collectors.toMap(EmailsDaTransacaoTest::destinatario, Function.identity()));
    }

    private static String destinatario(MimeMessage mensagem) {
        try {
            return Arrays.stream(mensagem.getRecipients(RecipientType.TO))
                    .map(endereco -> ((InternetAddress) endereco).getAddress())
                    .collect(Collectors.joining(","));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private static String html(MimeMessage mensagem) throws Exception {
        return (String) mensagem.getContent();
    }

    @Test
    @DisplayName("Envio confirmado: um e-mail ao aluno e outro ao professor, cada um com o seu template")
    void doisTemplates() throws Exception {
        transacoes.enviar(helena, new TransacaoRequest(ana, 120, "Liderou o grupo no projeto final."));

        Map<String, MimeMessage> emails = doisEmails();
        assertThat(emails).containsOnlyKeys(EMAIL_ANA, EMAIL_HELENA);

        MimeMessage aluno = emails.get(EMAIL_ANA);
        assertThat(aluno.getSubject()).isEqualTo("Você recebeu 120 Egos de Helena Duarte");
        assertThat(html(aluno))
                .contains("Moedas recebidas", "Ana Souza", "Helena Duarte", "120 Egos", "Liderou o grupo no projeto final.")
                .contains("Seu saldo agora", "VER MEU SALDO E AS VANTAGENS")
                .doesNotContain("Envio confirmado");

        MimeMessage professor = emails.get(EMAIL_HELENA);
        assertThat(professor.getSubject()).isEqualTo("Envio confirmado: 120 Egos para Ana Souza");
        assertThat(html(professor))
                .contains("Envio confirmado", "Ana Souza", "Helena Duarte", "120 Egos", "Liderou o grupo no projeto final.")
                .contains("Saldo para distribuir", EmailsDaTransacao.egos(saldoHelena - 120), "VER MEU EXTRATO")
                .doesNotContain("Moedas recebidas");
    }

    @Test
    @DisplayName("Os e-mails levam o saldo de cada um depois do envio, a data e o código da transação")
    void saldosDataECodigo() throws Exception {
        UUID id = transacoes.enviar(helena, new TransacaoRequest(ana, 1, "Pontualidade.")).id();

        Map<String, MimeMessage> emails = doisEmails();
        assertThat(html(emails.get(EMAIL_ANA))).contains("1 Ego<", id.toString()).containsPattern("\\d{2}/\\d{2}/\\d{4} às \\d{2}:\\d{2}");
        assertThat(html(emails.get(EMAIL_HELENA)))
                .contains(EmailsDaTransacao.egos(saldoHelena - 1), id.toString());
        assertThat(emails.get(EMAIL_ANA).getSubject()).isEqualTo("Você recebeu 1 Ego de Helena Duarte");
    }

    @Test
    @DisplayName("O motivo escrito pelo professor entra como texto, nunca como HTML")
    void motivoEscapado() throws Exception {
        transacoes.enviar(helena, new TransacaoRequest(ana, 10, "<b>Ótimo</b> & <script>alert(1)</script>"));

        String html = html(doisEmails().get(EMAIL_ANA));
        assertThat(html).contains("&lt;b&gt;Ótimo&lt;/b&gt; &amp; &lt;script&gt;").doesNotContain("<script>");
    }

    @Test
    @DisplayName("Envio recusado não manda e-mail nenhum")
    void recusadoSemEmail() {
        assertThatThrownBy(() -> transacoes.enviar(helena, new TransacaoRequest(ana, saldoHelena + 1, "Muito.")))
                .isInstanceOf(ApiException.class);

        verify(smtp, after(1500).never()).send(any(MimeMessage.class));
    }

    @Test
    @DisplayName("Quantias como o app escreve: singular, plural e milhar")
    void quantias() {
        assertThat(EmailsDaTransacao.egos(1)).isEqualTo("1 Ego");
        assertThat(EmailsDaTransacao.egos(2)).isEqualTo("2 Egos");
        assertThat(EmailsDaTransacao.egos(1000)).isEqualTo("1.000 Egos");
    }

}
