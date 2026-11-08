package com.egon.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.IntStream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import com.egon.Dados;
import com.egon.dto.AlunoAtualizacaoRequest;
import com.egon.dto.AlunoCadastroRequest;
import com.egon.dto.TransacaoRequest;
import com.egon.exception.ApiException;
import com.egon.model.Professor;
import com.egon.model.Transacao;
import com.egon.repository.AlunoRepository;
import com.egon.repository.CarteiraRepository;
import com.egon.repository.CursoRepository;
import com.egon.repository.InstituicaoRepository;
import com.egon.repository.ProfessorRepository;
import com.egon.repository.TransacaoRepository;

/**
 * Envios ao mesmo tempo, cada um na sua transação de verdade. Por isso não há
 * {@code @Transactional} aqui: o que o teste grava é apagado no fim, e os saldos voltam ao que eram.
 */
@SpringBootTest
class TransacaoConcorrenciaTest {

    @Autowired TransacaoService servico;
    @Autowired AlunoService alunoService;
    @Autowired AlunoRepository alunos;
    @Autowired CursoRepository cursos;
    @Autowired InstituicaoRepository instituicoes;
    @Autowired ProfessorRepository professores;
    @Autowired TransacaoRepository transacoes;
    @Autowired CarteiraRepository carteiras;
    @Autowired PlatformTransactionManager gerenciadorDeTransacoes;

    private UUID helena; // Universidade Horizonte, saldo 1000
    private UUID ricardo; // Universidade Horizonte, saldo 1000
    private UUID horizonte;
    private UUID ana;
    private int saldoHelena;
    private int saldoRicardo;

    @BeforeEach
    void preparar() {
        helena = professores.findByCpf(Dados.HELENA).orElseThrow().getId();
        ricardo = professores.findByCpf(Dados.RICARDO).orElseThrow().getId();
        horizonte = instituicoes.findByCnpj(Dados.HORIZONTE).orElseThrow().getId();
        saldoHelena = saldoDe(helena);
        saldoRicardo = saldoDe(ricardo);
        ana = alunoService.cadastrar(new AlunoCadastroRequest("Ana Souza", "ana@email.com", Dados.ANA, "123",
                "Rua A, 1", horizonte, cursos.findAllByOrderByNomeAsc().getFirst().getId(), "ana", "segredo123")).id();
    }

    @AfterEach
    void limpar() {
        transacoes.deleteAll(transacoes.findByAlunoIdOrderByDataDesc(ana));
        carteiras.deleteAll(carteiras.findByAlunoIdOrderByProfessorNomeAsc(ana));
        alunos.deleteById(ana);
        restaurar(helena, saldoHelena);
        restaurar(ricardo, saldoRicardo);
    }

    private void restaurar(UUID id, int saldo) {
        Professor professor = professores.findById(id).orElseThrow();
        professor.setSaldo(saldo);
        professores.save(professor);
    }

    private int saldoDe(UUID professor) {
        return professores.findById(professor).orElseThrow().getSaldo();
    }

    private int saldoDaAna() {
        return carteiras.somaPorAluno(ana);
    }

    private int somaEnviadaPor(UUID professor) {
        return transacoes.findByProfessorIdOrderByDataDesc(professor).stream().mapToInt(Transacao::getValor).sum();
    }

    /** Solta todos os envios juntos e devolve, para cada um, "OK" ou o código do erro de negócio. */
    private List<String> simultaneos(List<Callable<?>> envios) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(envios.size());
        CountDownLatch largada = new CountDownLatch(1);
        try {
            List<Future<String>> resultados = envios.stream().map(envio -> executor.submit(() -> {
                largada.await();
                try {
                    envio.call();
                    return "OK";
                } catch (ApiException e) {
                    return e.getCodigo();
                }
            })).toList();
            largada.countDown();
            List<String> codigos = new ArrayList<>();
            for (Future<String> resultado : resultados) {
                codigos.add(resultado.get(30, TimeUnit.SECONDS));
            }
            return codigos;
        } finally {
            executor.shutdownNow();
        }
    }

    /**
     * Roda {@code operacao} numa transação que só termina depois de um envio de 10 de Helena a Ana
     * ter tido a chance de acontecer no meio dela: até 500 ms, menos que a espera de trava do H2.
     */
    private Future<?> envioDurante(Runnable operacao) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        AtomicReference<Future<?>> envio = new AtomicReference<>();
        try {
            new TransactionTemplate(gerenciadorDeTransacoes).executeWithoutResult(status -> {
                operacao.run();
                envio.set(executor.submit(() -> servico.enviar(helena, new TransacaoRequest(ana, 10, "No meio"))));
                try {
                    envio.get().get(500, TimeUnit.MILLISECONDS);
                } catch (TimeoutException | ExecutionException e) {
                    // Parado na trava do aluno, ou recusado: quem chama confere o resultado.
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
            return envio.get();
        } finally {
            executor.shutdown();
        }
    }

    @Test
    @DisplayName("editar o aluno durante um envio não apaga o crédito que o envio fez")
    void edicaoDuranteEnvio() throws Exception {
        int curso = cursos.findAllByOrderByNomeAsc().getFirst().getId();

        Future<?> envio = envioDurante(() -> alunoService.atualizar(ana, new AlunoAtualizacaoRequest(
                "Ana Lima", "ana@email.com", "123", "Rua A, 1", horizonte, curso, "ana", null)));
        envio.get(30, TimeUnit.SECONDS);

        assertThat(saldoDaAna()).isEqualTo(10);
        assertThat(saldoDe(helena)).isEqualTo(saldoHelena - 10);
    }

    @Test
    @DisplayName("excluir o aluno durante um envio não esbarra no histórico: o envio espera e não acha mais o aluno")
    void exclusaoDuranteEnvio() {
        Future<?> envio = envioDurante(() -> alunoService.excluir(ana));

        assertThatThrownBy(() -> envio.get(30, TimeUnit.SECONDS)).cause()
                .isInstanceOf(ApiException.class)
                .hasFieldOrPropertyWithValue("codigo", "ALUNO_NAO_ENCONTRADO");
        assertThat(alunos.existsById(ana)).isFalse();
        assertThat(saldoDe(helena)).isEqualTo(saldoHelena);
    }

    @Test
    @DisplayName("envios simultâneos do mesmo professor nunca deixam o saldo negativo, e o débito é a soma do que foi gravado")
    void mesmoProfessor() throws Exception {
        // 20 envios de 70 contra um saldo de 1000: só 14 cabem.
        List<String> resultados = simultaneos(IntStream.range(0, 20)
                .<Callable<?>>mapToObj(i -> () -> servico.enviar(helena, new TransacaoRequest(ana, 70, "Envio " + i)))
                .toList());

        assertThat(resultados).filteredOn("OK"::equals).hasSize(14);
        assertThat(resultados).filteredOn("SALDO_INSUFICIENTE"::equals).hasSize(6);
        assertThat(saldoDe(helena)).isEqualTo(20);
        assertThat(saldoHelena - saldoDe(helena)).isEqualTo(somaEnviadaPor(helena));
        assertThat(saldoDaAna()).isEqualTo(980);
    }

    @Test
    @DisplayName("professores creditando o mesmo aluno ao mesmo tempo não perdem nenhum crédito, cada um na sua carteira")
    void mesmoAluno() throws Exception {
        List<String> resultados = simultaneos(IntStream.range(0, 20)
                .<Callable<?>>mapToObj(i -> () -> servico.enviar(i % 2 == 0 ? helena : ricardo,
                        new TransacaoRequest(ana, 10, "Envio " + i)))
                .toList());

        assertThat(resultados).containsOnly("OK");
        assertThat(saldoDaAna()).isEqualTo(200);
        assertThat(carteiras.findByAlunoIdOrderByProfessorNomeAsc(ana)).extracting("saldo").containsExactly(100, 100);
        assertThat(saldoDe(helena)).isEqualTo(saldoHelena - 100);
        assertThat(saldoDe(ricardo)).isEqualTo(saldoRicardo - 100);
        assertThat(somaEnviadaPor(helena) + somaEnviadaPor(ricardo)).isEqualTo(200);
    }

}
