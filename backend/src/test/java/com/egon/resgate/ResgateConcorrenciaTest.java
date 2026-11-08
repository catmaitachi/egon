package com.egon.resgate;

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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;

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

/** Resgates e confirmações ao mesmo tempo, cada um na sua transação de verdade; tudo é apagado no fim. */
@SpringBootTest
class ResgateConcorrenciaTest {

    @Autowired ResgateService resgates;
    @Autowired TransacaoService transacoes;
    @Autowired AlunoService alunoService;
    @Autowired EmpresaService empresas;
    @Autowired VantagemService vantagens;
    @Autowired AlunoRepository alunos;
    @Autowired ProfessorRepository professores;
    @Autowired InstituicaoRepository instituicoes;
    @Autowired CursoRepository cursos;
    @Autowired CarteiraRepository carteiras;
    @Autowired TransacaoRepository transacaoRepository;
    @Autowired CupomRepository cupons;
    @Autowired TrocaDebitoRepository debitos;
    @Autowired TrocaRepository trocas;

    private UUID helena;
    private UUID ricardo;
    private UUID ana;
    private UUID livraria;
    private UUID livro;
    private int saldoHelena;
    private int saldoRicardo;

    @BeforeEach
    void preparar() {
        helena = professores.findByCpf(Dados.HELENA).orElseThrow().getId();
        ricardo = professores.findByCpf(Dados.RICARDO).orElseThrow().getId();
        saldoHelena = professores.findById(helena).orElseThrow().getSaldo();
        saldoRicardo = professores.findById(ricardo).orElseThrow().getSaldo();
        ana = alunoService.cadastrar(new AlunoCadastroRequest("Ana Souza", "ana@email.com", Dados.ANA, "1", "Rua A",
                instituicoes.findByCnpj(Dados.HORIZONTE).orElseThrow().getId(),
                cursos.findAllByOrderByNomeAsc().getFirst().getId(), "ana", "segredo123")).id();
        transacoes.enviar(helena, new TransacaoRequest(ana, 100, "Monitoria"));
        transacoes.enviar(ricardo, new TransacaoRequest(ana, 60, "Participação"));
        livraria = empresas.cadastrar(new EmpresaCadastroRequest("Livraria", "45218073000192", "l@livraria.com",
                "livraria", "segredo123")).id();
        livro = vantagens.cadastrar(livraria, new VantagemRequest("Livro", "Um livro", 100, TipoVantagem.PRODUTO),
                new MockMultipartFile("foto", "f.png", "image/png", Dados.PNG)).id();
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
        restaurar(helena, saldoHelena);
        restaurar(ricardo, saldoRicardo);
    }

    private void restaurar(UUID id, int saldo) {
        Professor professor = professores.findById(id).orElseThrow();
        professor.setSaldo(saldo);
        professores.save(professor);
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
    @DisplayName("dois resgates de 100 com 160 de saldo: só um passa e as carteiras nunca ficam negativas")
    void resgatesSimultaneos() throws Exception {
        List<String> resultados = simultaneos(List.of(() -> resgates.resgatar(ana, livro), () -> resgates.resgatar(ana, livro)));

        assertThat(resultados).containsExactlyInAnyOrder("OK", "SALDO_INSUFICIENTE");
        assertThat(carteiras.somaPorAluno(ana)).isEqualTo(60);
        assertThat(trocas.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("duas confirmações do mesmo cupom ao mesmo tempo: uma vale, a outra é CUPOM_USADO")
    void confirmacoesSimultaneas() throws Exception {
        String codigo = resgates.resgatar(ana, livro).codigo();

        List<String> resultados = simultaneos(List.of(() -> resgates.confirmar(livraria, codigo),
                () -> resgates.confirmar(livraria, codigo)));

        assertThat(resultados).containsExactlyInAnyOrder("OK", "CUPOM_USADO");
    }

}
