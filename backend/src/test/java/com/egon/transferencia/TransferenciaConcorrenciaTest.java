package com.egon.transferencia;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.egon.Dados;
import com.egon.dto.AlunoCadastroRequest;
import com.egon.dto.RegrasRequest;
import com.egon.dto.TransacaoRequest;
import com.egon.dto.TransferenciaRequest;
import com.egon.exception.ApiException;
import com.egon.model.Professor;
import com.egon.model.StatusTransferencia;
import com.egon.repository.AlunoRepository;
import com.egon.repository.CarteiraRepository;
import com.egon.repository.CursoRepository;
import com.egon.repository.InstituicaoRepository;
import com.egon.repository.ProfessorRepository;
import com.egon.repository.TransacaoRepository;
import com.egon.repository.TransferenciaRepository;
import com.egon.security.ContaLogada;
import com.egon.security.Perfil;
import com.egon.service.AlunoService;
import com.egon.service.TransacaoService;
import com.egon.service.TransferenciaService;

/** Repasses ao mesmo tempo, cada um na sua transação de verdade; tudo é apagado no fim. */
@SpringBootTest
class TransferenciaConcorrenciaTest {

    @Autowired TransferenciaService servico;
    @Autowired TransacaoService transacoes;
    @Autowired AlunoService alunoService;
    @Autowired AlunoRepository alunos;
    @Autowired ProfessorRepository professores;
    @Autowired InstituicaoRepository instituicoes;
    @Autowired CursoRepository cursos;
    @Autowired CarteiraRepository carteiras;
    @Autowired TransacaoRepository transacaoRepository;
    @Autowired TransferenciaRepository transferencias;

    private UUID helena;
    private UUID ana;
    private UUID bruno;
    private int saldoHelena;

    @BeforeEach
    void preparar() {
        helena = professores.findByCpf(Dados.HELENA).orElseThrow().getId();
        saldoHelena = professores.findById(helena).orElseThrow().getSaldo();
        UUID horizonte = instituicoes.findByCnpj(Dados.HORIZONTE).orElseThrow().getId();
        int curso = cursos.findAllByOrderByNomeAsc().getFirst().getId();
        ana = alunoService.cadastrar(new AlunoCadastroRequest("Ana Souza", "ana@email.com", Dados.ANA, "1", "Rua A",
                horizonte, curso, "ana", "segredo123")).id();
        bruno = alunoService.cadastrar(new AlunoCadastroRequest("Bruno Lima", "bruno@email.com", Dados.BRUNO, "1",
                "Rua B", horizonte, curso, "bruno", "segredo123")).id();
        transacoes.enviar(helena, new TransacaoRequest(ana, 100, "Monitoria"));
    }

    @AfterEach
    void limpar() {
        transferencias.deleteAll();
        transacaoRepository.deleteAll(transacaoRepository.findByAlunoIdOrderByDataDesc(ana));
        carteiras.deleteAll(carteiras.findByAlunoIdOrderByProfessorNomeAsc(ana));
        carteiras.deleteAll(carteiras.findByAlunoIdOrderByProfessorNomeAsc(bruno));
        alunos.deleteById(ana);
        alunos.deleteById(bruno);
        Professor professor = professores.findById(helena).orElseThrow();
        professor.setSaldo(saldoHelena);
        professor.getRegras().setExigeAprovacao(true);
        professores.save(professor);
    }

    private int carteira(UUID aluno) {
        return carteiras.findByAlunoIdOrderByProfessorNomeAsc(aluno).stream()
                .filter(c -> c.getProfessor().getId().equals(helena)).mapToInt(c -> c.getSaldo()).sum();
    }

    private List<String> simultaneos(List<Callable<?>> chamadas) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(chamadas.size());
        CountDownLatch largada = new CountDownLatch(1);
        try {
            List<Future<String>> resultados = chamadas.stream().map(chamada -> executor.submit(() -> {
                largada.await();
                try {
                    chamada.call();
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

    @Test
    @DisplayName("20 repasses simultâneos de 10 com 100 na carteira: só 10 passam e a carteira nunca fica negativa")
    void pedidosSimultaneos() throws Exception {
        servico.definirRegras(helena, new RegrasRequest(true, false, null, null, false));

        List<String> resultados = simultaneos(IntStream.range(0, 20).<Callable<?>>mapToObj(i -> () ->
                servico.pedir(ana, new TransferenciaRequest(bruno, helena, 10, "Repasse " + i))).toList());

        assertThat(resultados).filteredOn("OK"::equals).hasSize(10);
        assertThat(resultados).filteredOn("SALDO_INSUFICIENTE"::equals).hasSize(10);
        assertThat(carteira(ana)).isZero();
        assertThat(carteira(bruno)).isEqualTo(100);
    }

    @Test
    @DisplayName("aprovar e cancelar a mesma pendente ao mesmo tempo: uma decisão vale, a outra dá 409, e os Egos ficam num lugar só")
    void decisoesSimultaneas() throws Exception {
        UUID id = servico.pedir(ana, new TransferenciaRequest(bruno, helena, 30, "Ajuda")).id();
        ContaLogada professora = new ContaLogada(helena, Perfil.PROFESSOR, "Helena", "helena.duarte", null);
        ContaLogada aluna = new ContaLogada(ana, Perfil.ALUNO, "Ana", "ana", null);

        List<String> resultados = simultaneos(List.of(
                () -> servico.decidir(id, StatusTransferencia.APROVADA, professora),
                () -> servico.decidir(id, StatusTransferencia.CANCELADA, aluna)));

        assertThat(resultados).containsExactlyInAnyOrder("OK", "TRANSFERENCIA_DECIDIDA");
        StatusTransferencia final_ = transferencias.findById(id).orElseThrow().getStatus();
        if (final_ == StatusTransferencia.APROVADA) {
            assertThat(carteira(ana)).isEqualTo(70);
            assertThat(carteira(bruno)).isEqualTo(30);
        } else {
            assertThat(final_).isEqualTo(StatusTransferencia.CANCELADA);
            assertThat(carteira(ana)).isEqualTo(100);
            assertThat(carteira(bruno)).isZero();
        }
    }

}
