package com.egon.notificacao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.net.CookieManager;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.egon.Dados;
import com.egon.dto.AlunoCadastroRequest;
import com.egon.dto.EmpresaCadastroRequest;
import com.egon.dto.VantagemRequest;
import com.egon.model.TipoVantagem;
import org.springframework.mock.web.MockMultipartFile;
import com.egon.dto.TransacaoRequest;
import com.egon.dto.TransacaoResponse;
import com.egon.dto.TransferenciaRequest;
import com.egon.exception.ApiException;
import com.egon.model.Professor;
import com.egon.model.StatusTransferencia;
import com.egon.security.ContaLogada;
import com.egon.security.Perfil;
import com.egon.repository.AlunoRepository;
import com.egon.repository.CarteiraRepository;
import com.egon.repository.InstituicaoRepository;
import com.egon.repository.CursoRepository;
import com.egon.repository.ProfessorRepository;
import com.egon.repository.CupomRepository;
import com.egon.repository.TransacaoRepository;
import com.egon.repository.TrocaDebitoRepository;
import com.egon.repository.TrocaRepository;
import com.egon.repository.TransferenciaRepository;
import com.egon.service.AlunoService;
import com.egon.service.EmpresaService;
import com.egon.service.ResgateService;
import com.egon.service.VantagemService;
import com.egon.service.TransacaoService;
import com.egon.service.TransferenciaService;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;

/**
 * Notificação ao vivo pelo WebSocket, com servidor de verdade. O envio commita de verdade (a
 * notificação só sai depois do commit), então nada aqui é {@code @Transactional}: o que o teste
 * grava é apagado no fim.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class NotificacaoWebSocketTest {


    @LocalServerPort int porta;
    @Autowired TransacaoService transacoes;
    @Autowired AlunoService alunoService;
    @Autowired EmpresaService empresas;
    @Autowired VantagemService vantagens;
    @Autowired ResgateService resgates;
    @Autowired CupomRepository cupons;
    @Autowired TrocaDebitoRepository debitos;
    @Autowired TrocaRepository trocas;
    @Autowired AlunoRepository alunos;
    @Autowired CursoRepository cursos;
    @Autowired ProfessorRepository professores;
    @Autowired TransacaoRepository transacaoRepository;
    @Autowired TransferenciaRepository transferenciaRepository;
    @Autowired TransferenciaService transferencias;
    @Autowired CarteiraRepository carteiras;
    @Autowired InstituicaoRepository instituicoes;
    @Autowired PlatformTransactionManager gerenciadorDeTransacoes;

    private final List<WebSocketSession> abertas = new ArrayList<>();
    private int saldoHelena;
    private UUID helena; // Universidade Horizonte
    private UUID beatriz; // Centro Universitário Vale Verde
    private UUID ana; // aluna da Universidade Horizonte

    @BeforeEach
    void preparar() {
        helena = professores.findByCpf(Dados.HELENA).orElseThrow().getId();
        beatriz = professores.findByCpf(Dados.BEATRIZ).orElseThrow().getId();
        saldoHelena = professores.findById(helena).orElseThrow().getSaldo();
        ana = alunoService.cadastrar(new AlunoCadastroRequest("Ana Souza", "ana@email.com", Dados.ANA, "123",
                "Rua A, 1", instituicoes.findByCnpj(Dados.HORIZONTE).orElseThrow().getId(),
                cursos.findAllByOrderByNomeAsc().getFirst().getId(), "ana", "segredo123")).id();
    }

    @AfterEach
    void limpar() throws IOException {
        for (WebSocketSession sessao : abertas) {
            sessao.close();
        }
        transacaoRepository.deleteAll(transacaoRepository.findByAlunoIdOrderByDataDesc(ana));
        carteiras.deleteAll(carteiras.findByAlunoIdOrderByProfessorNomeAsc(ana));
        alunos.deleteById(ana);
        Professor professor = professores.findById(helena).orElseThrow();
        professor.setSaldo(saldoHelena);
        professores.save(professor);
    }

    private URI endereco() {
        return URI.create("ws://localhost:" + porta + "/ws/notificacoes");
    }

    /**
     * Entra como o navegador faria (cookie de CSRF, login) e devolve o cabeçalho com a sessão,
     * que o handshake do WebSocket leva para dizer quem está conectando.
     */
    private WebSocketHttpHeaders sessao(String login, String senha) throws Exception {
        CookieManager cookies = new CookieManager();
        HttpClient cliente = HttpClient.newBuilder().cookieHandler(cookies).build();
        String base = "http://localhost:" + porta;
        cliente.send(HttpRequest.newBuilder(URI.create(base + "/auth/eu")).build(), BodyHandlers.discarding());
        HttpResponse<String> login1 = cliente.send(HttpRequest.newBuilder(URI.create(base + "/auth/login"))
                .header("Content-Type", "application/json")
                .header("X-XSRF-TOKEN", cookie(cookies, "XSRF-TOKEN"))
                .POST(BodyPublishers.ofString("{\"login\": \"%s\", \"senha\": \"%s\"}".formatted(login, senha)))
                .build(), BodyHandlers.ofString());
        assertThat(login1.statusCode()).as(login1.body()).isEqualTo(200);
        WebSocketHttpHeaders cabecalhos = new WebSocketHttpHeaders();
        cabecalhos.add("Cookie", "JSESSIONID=" + cookie(cookies, "JSESSIONID"));
        return cabecalhos;
    }

    private static String cookie(CookieManager cookies, String nome) {
        return cookies.getCookieStore().getCookies().stream()
                .filter(c -> c.getName().equals(nome)).reduce((primeiro, ultimo) -> ultimo).orElseThrow().getValue();
    }

    private WebSocketHttpHeaders comoAna() throws Exception {
        return sessao("ana", "segredo123");
    }

    private WebSocketHttpHeaders comoHelena() throws Exception {
        return sessao("helena.duarte", "professor123");
    }

    /** Conecta e devolve a fila onde caem as mensagens recebidas. */
    private BlockingQueue<String> conectar(WebSocketHttpHeaders cabecalhos) throws Exception {
        BlockingQueue<String> recebidas = new LinkedBlockingQueue<>();
        abertas.add(new StandardWebSocketClient().execute(new TextWebSocketHandler() {
            @Override
            protected void handleTextMessage(WebSocketSession sessao, TextMessage mensagem) {
                recebidas.add(mensagem.getPayload());
            }
        }, cabecalhos, endereco()).get(5, TimeUnit.SECONDS));
        // O servidor guarda a sessão logo depois de responder ao handshake; a espera cobre esse intervalo.
        Thread.sleep(200);
        return recebidas;
    }

    private static DocumentContext proxima(BlockingQueue<String> recebidas) throws InterruptedException {
        String mensagem = recebidas.poll(5, TimeUnit.SECONDS);
        assertThat(mensagem).as("mensagem recebida em 5 s").isNotNull();
        return JsonPath.parse(mensagem);
    }

    private TransacaoResponse enviar(UUID professor, int valor) {
        return transacoes.enviar(professor, new TransacaoRequest(ana, valor, "Monitoria"));
    }

    @Test
    @DisplayName("aluno e professor conectados recebem cada um sua mensagem, com a transação e o saldo novo")
    void cadaLadoRecebeASua() throws Exception {
        BlockingQueue<String> aluno = conectar(comoAna());
        BlockingQueue<String> professor = conectar(comoHelena());

        String id = enviar(helena, 300).id().toString();

        DocumentContext paraAluno = proxima(aluno);
        assertThat(paraAluno.read("$.tipo", String.class)).isEqualTo("MOEDAS_RECEBIDAS");
        assertThat(paraAluno.read("$.transacao.id", String.class)).isEqualTo(id);
        assertThat(paraAluno.read("$.transacao.valor", Integer.class)).isEqualTo(300);
        assertThat(paraAluno.read("$.transacao.motivo", String.class)).isEqualTo("Monitoria");
        assertThat(paraAluno.read("$.transacao.data", String.class)).endsWith("Z");
        assertThat(paraAluno.read("$.transacao.professor.nome", String.class)).isEqualTo("Helena Duarte");
        assertThat(paraAluno.read("$.transacao.aluno.id", String.class)).isEqualTo(ana.toString());
        assertThat(paraAluno.read("$.saldo", Integer.class)).isEqualTo(300);

        DocumentContext paraProfessor = proxima(professor);
        assertThat(paraProfessor.read("$.tipo", String.class)).isEqualTo("ENVIO_CONFIRMADO");
        assertThat(paraProfessor.read("$.transacao.id", String.class)).isEqualTo(id);
        assertThat(paraProfessor.read("$.transacao.aluno.nome", String.class)).isEqualTo("Ana Souza");
        assertThat(paraProfessor.read("$.saldo", Integer.class)).isEqualTo(saldoHelena - 300);

        assertThat(aluno).isEmpty();
        assertThat(professor).isEmpty();
    }

    @Test
    @DisplayName("as duas abas abertas do mesmo aluno recebem a mensagem")
    void variasAbas() throws Exception {
        WebSocketHttpHeaders ana = comoAna();
        BlockingQueue<String> primeira = conectar(ana);
        BlockingQueue<String> segunda = conectar(ana);

        enviar(helena, 50);

        assertThat(proxima(primeira).read("$.transacao.valor", Integer.class)).isEqualTo(50);
        assertThat(proxima(segunda).read("$.transacao.valor", Integer.class)).isEqualTo(50);
    }

    @Test
    @DisplayName("envio recusado pela validação não manda nada")
    void envioRecusadoNaoNotifica() throws Exception {
        BlockingQueue<String> aluno = conectar(comoAna());
        BlockingQueue<String> professor = conectar(comoHelena());

        assertThatThrownBy(() -> enviar(helena, 1_000_000)).isInstanceOf(ApiException.class);
        assertThatThrownBy(() -> enviar(beatriz, 10)).isInstanceOf(ApiException.class);
        enviar(helena, 10);

        // Se um envio recusado tivesse mandado algo, chegaria antes deste, pela mesma conexão.
        assertThat(proxima(aluno).read("$.transacao.valor", Integer.class)).isEqualTo(10);
        assertThat(proxima(professor).read("$.transacao.valor", Integer.class)).isEqualTo(10);
    }

    @Test
    @DisplayName("nada é enviado se a transação for desfeita depois de o envio ser registrado")
    void rollbackNaoNotifica() throws Exception {
        BlockingQueue<String> aluno = conectar(comoAna());

        new TransactionTemplate(gerenciadorDeTransacoes).executeWithoutResult(status -> {
            enviar(helena, 111);
            status.setRollbackOnly();
        });
        assertThat(carteiras.somaPorAluno(ana)).isZero();
        enviar(helena, 222);

        assertThat(proxima(aluno).read("$.transacao.valor", Integer.class)).isEqualTo(222);
    }

    @Test
    @DisplayName("repasse: o pedido avisa o professor e quem pediu; a aprovação, quem pediu e quem recebe; o cancelamento, o professor")
    void avisosDeRepasse() throws Exception {
        UUID bruno = alunoService.cadastrar(new AlunoCadastroRequest("Bruno Lima", "bruno@email.com", Dados.BRUNO, "1",
                "Rua B", instituicoes.findByCnpj(Dados.HORIZONTE).orElseThrow().getId(),
                cursos.findAllByOrderByNomeAsc().getFirst().getId(), "bruno", "segredo123")).id();
        try {
            enviar(helena, 100);
            BlockingQueue<String> aluna = conectar(comoAna());
            BlockingQueue<String> professora = conectar(comoHelena());
            BlockingQueue<String> colega = conectar(sessao("bruno", "segredo123"));

            UUID pedido = transferencias.pedir(ana, new TransferenciaRequest(bruno, helena, 30, "Ajuda")).id();
            DocumentContext paraProfessora = proxima(professora);
            assertThat(paraProfessora.read("$.tipo", String.class)).isEqualTo("TRANSFERENCIA");
            assertThat(paraProfessora.read("$.evento", String.class)).isEqualTo("SOLICITADA");
            assertThat(paraProfessora.read("$.transferencia.id", String.class)).isEqualTo(pedido.toString());
            DocumentContext paraAluna = proxima(aluna);
            assertThat(paraAluna.read("$.evento", String.class)).isEqualTo("SOLICITADA");
            assertThat(paraAluna.read("$.saldo", Integer.class)).isEqualTo(70);

            transferencias.decidir(pedido, StatusTransferencia.APROVADA,
                    new ContaLogada(helena, Perfil.PROFESSOR, "Helena", "helena.duarte", null));
            assertThat(proxima(aluna).read("$.evento", String.class)).isEqualTo("APROVADA");
            DocumentContext paraColega = proxima(colega);
            assertThat(paraColega.read("$.evento", String.class)).isEqualTo("APROVADA");
            assertThat(paraColega.read("$.saldo", Integer.class)).isEqualTo(30);

            UUID outro = transferencias.pedir(ana, new TransferenciaRequest(bruno, helena, 10, "Outra")).id();
            proxima(professora);
            proxima(aluna);
            transferencias.decidir(outro, StatusTransferencia.CANCELADA,
                    new ContaLogada(ana, Perfil.ALUNO, "Ana", "ana", null));
            assertThat(proxima(professora).read("$.evento", String.class)).isEqualTo("CANCELADA");
            assertThat(colega).isEmpty();
        } finally {
            transferenciaRepository.deleteAll();
            carteiras.deleteAll(carteiras.findByAlunoIdOrderByProfessorNomeAsc(bruno));
            alunos.deleteById(bruno);
        }
    }

    private void recusa(WebSocketHttpHeaders cabecalhos, String quem) {
        assertThatThrownBy(() -> new StandardWebSocketClient()
                .execute(new TextWebSocketHandler(), cabecalhos, endereco())
                .get(5, TimeUnit.SECONDS))
                .as(quem)
                .isInstanceOf(ExecutionException.class);
    }

    @Test
    @DisplayName("recusa o handshake sem sessão, com sessão inventada e do admin, que não recebe aviso")
    void recusaHandshake() throws Exception {
        recusa(new WebSocketHttpHeaders(), "sem sessão");
        WebSocketHttpHeaders inventada = new WebSocketHttpHeaders();
        inventada.add("Cookie", "JSESSIONID=nao-existe");
        recusa(inventada, "sessão inventada");
        recusa(sessao("admin", "admin12345"), "admin");
    }

    @Test
    @DisplayName("cupom: o resgate avisa o aluno e a empresa; a confirmação na loja avisa o aluno")
    void avisosDeCupom() throws Exception {
        UUID livraria = empresas.cadastrar(new EmpresaCadastroRequest("Livraria", "45218073000192",
                "contato@livraria.com", "livraria", "segredo123")).id();
        try {
            UUID livro = vantagens.cadastrar(livraria, new VantagemRequest("Livro", "Um livro", 40, TipoVantagem.PRODUTO),
                    new MockMultipartFile("foto", "f.png", "image/png", Dados.PNG)).id();
            enviar(helena, 100);
            BlockingQueue<String> aluna = conectar(comoAna());
            BlockingQueue<String> loja = conectar(sessao("livraria", "segredo123"));

            String codigo = resgates.resgatar(ana, livro).codigo();
            DocumentContext paraAluna = proxima(aluna);
            assertThat(paraAluna.read("$.tipo", String.class)).isEqualTo("CUPOM");
            assertThat(paraAluna.read("$.evento", String.class)).isEqualTo("EMITIDO");
            assertThat(paraAluna.read("$.cupom.codigo", String.class)).isEqualTo(codigo);
            assertThat(paraAluna.read("$.saldo", Integer.class)).isEqualTo(60);
            DocumentContext paraLoja = proxima(loja);
            assertThat(paraLoja.read("$.evento", String.class)).isEqualTo("EMITIDO");
            assertThat(paraLoja.read("$.cupom.aluno.nome", String.class)).isEqualTo("Ana Souza");

            resgates.confirmar(livraria, codigo);
            assertThat(proxima(aluna).read("$.evento", String.class)).isEqualTo("USADO");
            assertThat(loja).isEmpty();
        } finally {
            cupons.deleteAll();
            debitos.deleteAll();
            trocas.deleteAll();
            empresas.excluir(livraria);
        }
    }

    @Test
    @DisplayName("aceita a origem do front em dev e recusa as que não estão configuradas")
    void origens() throws Exception {
        WebSocketHttpHeaders doFront = comoAna();
        doFront.setOrigin("http://localhost:5173");
        conectar(doFront);

        WebSocketHttpHeaders deFora = comoAna();
        deFora.setOrigin("http://exemplo.com");
        assertThatThrownBy(() -> new StandardWebSocketClient()
                .execute(new TextWebSocketHandler(), deFora, endereco())
                .get(5, TimeUnit.SECONDS))
                .isInstanceOf(ExecutionException.class);
    }

}
