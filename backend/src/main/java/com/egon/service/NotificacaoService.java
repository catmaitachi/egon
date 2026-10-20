package com.egon.service;

import java.text.NumberFormat;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.egon.auth.UsuarioLogado;
import com.egon.dto.NotificacaoResponse;
import com.egon.model.Aluno;
import com.egon.model.Cupom;
import com.egon.model.Empresa;
import com.egon.model.Notificacao;
import com.egon.model.Transacao;
import com.egon.model.Troca;
import com.egon.model.Vantagem;
import com.egon.repository.NotificacaoRepository;

/**
 * Os e-mails do sistema (HU07, HU10). Cada um fica registrado em NOTIFICACAO, que aluno e
 * empresa leem no painel, e sai por SMTP depois do commit ({@link EnvioDeEmail}): uma operação
 * desfeita não manda e-mail de algo que não aconteceu.
 */
@Service
public class NotificacaoService {

    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm");
    private static final NumberFormat NUMERO = NumberFormat.getIntegerInstance(Locale.of("pt", "BR"));

    private final NotificacaoRepository notificacoes;
    private final ApplicationEventPublisher eventos;
    private final Clock relogio;
    private final String moeda;

    public NotificacaoService(NotificacaoRepository notificacoes, ApplicationEventPublisher eventos, Clock relogio,
            @Value("${egon.moeda:Ego}") String moeda) {
        this.notificacoes = notificacoes;
        this.eventos = eventos;
        this.relogio = relogio;
        this.moeda = moeda;
    }

    /** HU07: professor, quantidade, data e a mensagem na íntegra. */
    @Transactional(propagation = Propagation.MANDATORY)
    public void moedasRecebidas(Transacao transacao) {
        Aluno aluno = transacao.getAluno();
        String professor = transacao.getProfessor().getNome();
        String corpo = """
                Olá, %s!

                %s reconheceu você com %s em %s.

                Mensagem do professor:
                "%s"

                Seu saldo agora é de %s.
                """.formatted(aluno.getNome(), professor, moedas(transacao.getValor()),
                transacao.getData().format(DATA), transacao.getMotivo(), moedas(aluno.getSaldo()));
        registrar(aluno.getEmail(), "Você recebeu " + moedas(transacao.getValor()) + " de " + professor, corpo,
                aluno, null, null);
    }

    /** HU10: o mesmo código vai para o aluno e para a empresa, cada um com o que precisa saber. */
    @Transactional(propagation = Propagation.MANDATORY)
    public void trocaRealizada(Troca troca, Cupom cupom) {
        Aluno aluno = troca.getAluno();
        Vantagem vantagem = troca.getVantagem();
        Empresa empresa = vantagem.getEmpresa();

        String paraAluno = """
                Olá, %s!

                Sua troca foi confirmada. Apresente este código à empresa parceira para usar a vantagem.

                Código do cupom: %s
                Vantagem: %s
                Empresa parceira: %s
                Valor debitado: %s

                Seu saldo agora é de %s.
                """.formatted(aluno.getNome(), cupom.getCodigo(), vantagem.getNome(),
                empresa.getNome(), moedas(troca.getValor()), moedas(aluno.getSaldo()));
        registrar(aluno.getEmail(), "Cupom " + cupom.getCodigo() + ": " + vantagem.getNome(), paraAluno,
                aluno, null, cupom);

        String paraEmpresa = """
                Olá, %s!

                Um aluno resgatou uma das suas vantagens.

                Código do cupom: %s
                Vantagem: %s
                %s

                Aluno: %s (%s)
                Curso: %s
                Instituição: %s

                Na troca presencial, confira se o código apresentado pelo aluno é o mesmo deste e-mail.
                """.formatted(empresa.getNome(), cupom.getCodigo(), vantagem.getNome(), vantagem.getDescricao(),
                aluno.getNome(), aluno.getEmail(), aluno.getCurso().getNome(), aluno.getInstituicao().getNome());
        registrar(empresa.getEmail(), "Novo resgate: " + vantagem.getNome() + " (cupom " + cupom.getCodigo() + ")",
                paraEmpresa, null, empresa, cupom);
    }

    /** Os e-mails recebidos por quem está logado, do mais recente ao mais antigo. Professor não recebe e-mails. */
    @Transactional(readOnly = true)
    public List<NotificacaoResponse> listar(UsuarioLogado usuario) {
        List<Notificacao> lista = switch (usuario.perfil()) {
            case ALUNO -> notificacoes.findByAlunoCpfOrderByDataDescIdDesc(usuario.id());
            case EMPRESA -> notificacoes.findByEmpresaCnpjOrderByDataDescIdDesc(usuario.id());
            case PROFESSOR -> List.of();
        };
        return lista.stream().map(NotificacaoResponse::de).toList();
    }

    private void registrar(String email, String assunto, String corpo, Aluno aluno, Empresa empresa, Cupom cupom) {
        Notificacao notificacao = new Notificacao();
        notificacao.setAssunto(assunto.length() > 150 ? assunto.substring(0, 149) + "…" : assunto);
        notificacao.setDescricao(corpo.strip());
        notificacao.setData(LocalDateTime.now(relogio));
        notificacao.setAluno(aluno);
        notificacao.setEmpresa(empresa);
        notificacao.setCupom(cupom);
        notificacoes.save(notificacao);
        eventos.publishEvent(new EmailPendente(email, notificacao.getAssunto(), notificacao.getDescricao()));
    }

    private String moedas(int quantidade) {
        return NUMERO.format(quantidade) + " " + moeda + (quantidade == 1 ? "" : "s");
    }

}
