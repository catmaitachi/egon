package com.egon.service;

import java.time.Clock;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.egon.dto.ResumoResponse;
import com.egon.dto.ResumoResponse.Contagens;
import com.egon.dto.ResumoResponse.Egos;
import com.egon.dto.ResumoResponse.EnvioMes;
import com.egon.dto.ResumoResponse.PorInstituicao;
import com.egon.model.Transacao;
import com.egon.repository.AlunoRepository;
import com.egon.repository.CarteiraRepository;
import com.egon.repository.CotaSemestralRepository;
import com.egon.repository.CursoRepository;
import com.egon.repository.EmpresaRepository;
import com.egon.repository.InstituicaoRepository;
import com.egon.repository.ProfessorRepository;
import com.egon.repository.TransacaoRepository;
import com.egon.repository.TransferenciaRepository;
import com.egon.repository.TrocaRepository;
import com.egon.repository.VantagemRepository;

import lombok.RequiredArgsConstructor;

/** Os números do painel do admin, todos tirados das tabelas na hora. */
@Service
@RequiredArgsConstructor
public class ResumoService {

    private static final ZoneId FUSO = ZoneId.of("America/Sao_Paulo");
    private static final int MESES = 6;

    private final CotaSemestralRepository cotas;
    private final ProfessorRepository professores;
    private final CarteiraRepository carteiras;
    private final TransferenciaRepository transferencias;
    private final TransacaoRepository transacoes;
    private final InstituicaoRepository instituicoes;
    private final AlunoRepository alunos;
    private final CursoRepository cursos;
    private final EmpresaRepository empresas;
    private final VantagemRepository vantagens;
    private final TrocaRepository trocas;
    private final Clock relogio;

    @Transactional(readOnly = true)
    public ResumoResponse resumo() {
        Egos egos = new Egos(cotas.somaEmitida(), professores.somaSaldos(), carteiras.somaTotal(),
                transferencias.somaPendente(), trocas.somaResgatada());
        Contagens contagens = new Contagens(instituicoes.count(), cursos.count(), alunos.count(), professores.count(),
                empresas.count(), vantagens.count());
        return new ResumoResponse(egos, enviosPorMes(), porInstituicao(), contagens);
    }

    /** Os últimos 6 meses, o atual por último, com zero onde não houve envio. */
    private List<EnvioMes> enviosPorMes() {
        YearMonth atual = YearMonth.now(relogio.withZone(FUSO));
        YearMonth primeiro = atual.minusMonths(MESES - 1);
        Map<YearMonth, List<Transacao>> porMes = transacoes
                .findByDataGreaterThanEqual(primeiro.atDay(1).atStartOfDay(FUSO).toInstant()).stream()
                .collect(Collectors.groupingBy(t -> YearMonth.from(t.getData().atZone(FUSO))));
        return IntStream.range(0, MESES).mapToObj(primeiro::plusMonths).map(mes -> {
            List<Transacao> doMes = porMes.getOrDefault(mes, List.of());
            return new EnvioMes(mes.toString(), doMes.size(), doMes.stream().mapToLong(Transacao::getValor).sum());
        }).toList();
    }

    private List<PorInstituicao> porInstituicao() {
        Map<UUID, Long> alunosPor = pares(alunos.contarPorInstituicao());
        Map<UUID, Long> professoresPor = pares(professores.contarPorInstituicao());
        Map<UUID, Long> enviadoPor = pares(transacoes.enviadoPorInstituicao());
        return instituicoes.findAllByOrderByNomeAsc().stream()
                .map(i -> new PorInstituicao(i.getId(), i.getNome(), alunosPor.getOrDefault(i.getId(), 0L),
                        professoresPor.getOrDefault(i.getId(), 0L), enviadoPor.getOrDefault(i.getId(), 0L)))
                .toList();
    }

    private static Map<UUID, Long> pares(List<Object[]> linhas) {
        return linhas.stream().collect(Collectors.toMap(l -> (UUID) l[0], l -> ((Number) l[1]).longValue()));
    }

}
