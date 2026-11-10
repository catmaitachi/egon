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
import java.util.Properties;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.egon.Dados;
import com.egon.dto.AlunoCadastroRequest;
import com.egon.dto.EmpresaCadastroRequest;
import com.egon.dto.TransacaoRequest;
import com.egon.dto.VantagemRequest;
import com.egon.exception.ApiException;
import com.egon.model.Professor;
import com.egon.model.TipoVantagem;
import com.egon.repository.AlunoRepository;
import com.egon.repository.CarteiraRepository;
import com.egon.repository.CupomRepository;
import com.egon.repository.CursoRepository;
import com.egon.repository.InstituicaoRepository;
import com.egon.repository.ProfessorRepository;
import com.egon.repository.TransacaoRepository;
import com.egon.repository.TrocaDebitoRepository;
import com.egon.repository.TrocaRepository;
import com.egon.service.AlunoService;
import com.egon.service.EmpresaService;
import com.egon.service.ResgateService;
import com.egon.service.TransacaoService;
import com.egon.service.VantagemService;

import jakarta.mail.Message.RecipientType;
import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

/**
 * HU10 por e-mail: o resgate manda o cupom ao aluno e à empresa, com o mesmo código. O servidor SMTP é um dublê.
 *
 * <p>Os e-mails só saem depois do commit, então nada aqui é {@code @Transactional}: o que o teste grava é apagado
 * no fim. Os dois e-mails do envio de moedas que dá saldo à aluna também passam pelo dublê e são separados pelo
 * assunto.
 */
@SpringBootTest
class EmailsDoCupomTest {

    private static final String EMAIL_ANA = "ana@email.com";
    private static final String EMAIL_LIVRARIA = "contato@livraria.com";

    @MockitoBean JavaMailSender smtp;
    @Autowired TransacaoService transacoes;
    @Autowired AlunoService alunoService;
    @Autowired EmpresaService empresas;
    @Autowired VantagemService vantagens;
    @Autowired ResgateService resgates;
    @Autowired AlunoRepository alunos;
    @Autowired ProfessorRepository professores;
    @Autowired TransacaoRepository transacaoRepository;
    @Autowired CarteiraRepository carteiras;
    @Autowired CupomRepository cupons;
    @Autowired TrocaDebitoRepository debitos;
    @Autowired TrocaRepository trocas;
    @Autowired CursoRepository cursos;
    @Autowired InstituicaoRepository instituicoes;

    private UUID helena;
    private UUID ana;
    private UUID livraria;
    private UUID livro;
    private int saldoHelena;

    @BeforeEach
    void preparar() {
        when(smtp.createMimeMessage()).thenAnswer(chamada -> new MimeMessage(Session.getInstance(new Properties())));
        helena = professores.findByCpf(Dados.HELENA).orElseThrow().getId();
        saldoHelena = professores.findById(helena).orElseThrow().getSaldo();
        ana = alunoService.cadastrar(new AlunoCadastroRequest("Ana Souza", EMAIL_ANA, Dados.ANA, "123",
                "Rua A, 1", instituicoes.findByCnpj(Dados.HORIZONTE).orElseThrow().getId(),
                cursos.findAllByOrderByNomeAsc().getFirst().getId(), "ana", "segredo123")).id();
        livraria = empresas.cadastrar(new EmpresaCadastroRequest("Livraria Página Viva", "45218073000192",
                EMAIL_LIVRARIA, "livraria", "segredo123")).id();
        livro = vantagens.cadastrar(livraria, new VantagemRequest("Livro técnico", "Um livro da seção de tecnologia.", 40,
                TipoVantagem.PRODUTO), new MockMultipartFile("foto", "f.png", "image/png", Dados.PNG)).id();
    }

    @AfterEach
    void limpar() {
        cupons.deleteAll();
        debitos.deleteAll();
        trocas.deleteAll();
        empresas.excluir(livraria);
        transacaoRepository.deleteAll(transacaoRepository.findByAlunoIdOrderByDataDesc(ana));
        carteiras.deleteAll(carteiras.findByAlunoIdOrderByProfessorNomeAsc(ana));
        alunos.deleteById(ana);
        Professor professor = professores.findById(helena).orElseThrow();
        professor.setSaldo(saldoHelena);
        professores.save(professor);
    }

    /** Espera {@code total} e-mails (saem numa thread à parte) e devolve só os do cupom. */
    private List<MimeMessage> emailsDoCupom(int total) throws Exception {
        ArgumentCaptor<MimeMessage> enviados = ArgumentCaptor.forClass(MimeMessage.class);
        verify(smtp, timeout(5000).times(total)).send(enviados.capture());
        return enviados.getAllValues().stream().filter(m -> {
            try {
                return m.getSubject().contains("cupom") || m.getSubject().startsWith("Seu cupom");
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }).toList();
    }

    private static String para(MimeMessage mensagem) throws Exception {
        return Arrays.stream(mensagem.getRecipients(RecipientType.TO))
                .map(endereco -> ((InternetAddress) endereco).getAddress()).collect(Collectors.joining(","));
    }

    private static MimeMessage de(List<MimeMessage> emails, String destinatario) throws Exception {
        for (MimeMessage m : emails) {
            if (para(m).equals(destinatario)) {
                return m;
            }
        }
        throw new AssertionError("sem e-mail para " + destinatario);
    }

    @Test
    @DisplayName("Resgate: o cupom vai por e-mail ao aluno e à empresa, os dois com o mesmo código")
    void cupomParaOsDois() throws Exception {
        transacoes.enviar(helena, new TransacaoRequest(ana, 100, "Monitoria."));
        String codigo = resgates.resgatar(ana, livro).codigo();

        List<MimeMessage> emails = emailsDoCupom(4);
        assertThat(emails).hasSize(2);

        MimeMessage aluno = de(emails, EMAIL_ANA);
        assertThat(aluno.getSubject()).isEqualTo("Seu cupom " + codigo + ": Livro técnico");
        assertThat((String) aluno.getContent())
                .contains("Cupom de troca", codigo, "Ana Souza", "Livro técnico", "Livraria Página Viva", "40 Egos")
                .contains("Seu saldo agora", "60 Egos", "Um livro da seção de tecnologia.");

        MimeMessage empresa = de(emails, EMAIL_LIVRARIA);
        assertThat(empresa.getSubject()).isEqualTo("Novo resgate: Livro técnico (cupom " + codigo + ")");
        assertThat((String) empresa.getContent())
                .contains("Novo resgate", codigo, "Ana Souza", EMAIL_ANA, "Universidade Horizonte", "40 Egos")
                .contains("/painel?cupom=" + codigo)
                .doesNotContain("Seu saldo agora");
    }

    @Test
    @DisplayName("Confirmar a troca na loja não manda e-mail: só o aviso no app")
    void confirmacaoSemEmail() throws Exception {
        transacoes.enviar(helena, new TransacaoRequest(ana, 100, "Monitoria."));
        String codigo = resgates.resgatar(ana, livro).codigo();
        emailsDoCupom(4);

        resgates.confirmar(livraria, codigo);

        verify(smtp, after(1500).times(4)).send(any(MimeMessage.class));
    }

    @Test
    @DisplayName("Resgate recusado por saldo não manda e-mail de cupom")
    void recusadoSemEmail() {
        assertThatThrownBy(() -> resgates.resgatar(ana, livro)).isInstanceOf(ApiException.class);

        verify(smtp, after(1500).never()).send(any(MimeMessage.class));
    }

}
