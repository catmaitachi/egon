package com.egon.service;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.egon.dto.CupomResponse;
import com.egon.exception.ApiException;
import com.egon.model.Aluno;
import com.egon.model.Carteira;
import com.egon.model.Cupom;
import com.egon.model.StatusCupom;
import com.egon.model.Troca;
import com.egon.model.TrocaDebito;
import com.egon.model.Vantagem;
import com.egon.repository.AlunoRepository;
import com.egon.repository.CarteiraRepository;
import com.egon.repository.CupomRepository;
import com.egon.repository.ProfessorRepository;
import com.egon.repository.TrocaDebitoRepository;
import com.egon.repository.TrocaRepository;
import com.egon.repository.VantagemRepository;
import com.egon.security.Perfil;
import com.egon.service.TransferenciaNotificada.Aviso;

import lombok.RequiredArgsConstructor;

/**
 * HU09 e HU10: o aluno troca Egos por uma vantagem e recebe um cupom; a empresa confere o código e
 * confirma o uso na loja.
 *
 * <p>O custo sai das carteiras do aluno, a de maior saldo primeiro, e o quanto saiu de cada uma
 * fica registrado. Os professores dessas carteiras são travados em ordem de id antes das carteiras
 * (a mesma ordem professor → carteira de envios e repasses): dois resgates ao mesmo tempo não gastam
 * os mesmos Egos e ninguém espera em círculo.
 */
@Service
@RequiredArgsConstructor
public class ResgateService {

    private final VantagemRepository vantagens;
    private final AlunoRepository alunos;
    private final ProfessorRepository professores;
    private final CarteiraRepository carteiras;
    private final TrocaRepository trocas;
    private final TrocaDebitoRepository debitos;
    private final CupomRepository cupons;
    private final Clock relogio;
    private final ApplicationEventPublisher eventos;

    @Transactional
    public CupomResponse resgatar(UUID idAluno, UUID idVantagem) {
        Vantagem vantagem = vantagens.findById(idVantagem).orElseThrow(ApiException::vantagemNaoEncontrada);
        Aluno aluno = alunos.findById(idAluno).orElseThrow(ApiException::alunoNaoEncontrado);

        List<Carteira> doAluno = new ArrayList<>();
        for (UUID professor : carteiras.professoresDoAluno(idAluno).stream().sorted().toList()) {
            professores.findBloqueadoById(professor).orElseThrow();
            doAluno.add(carteiras.findBloqueadaByAlunoIdAndProfessorId(idAluno, professor).orElseThrow());
        }
        int total = doAluno.stream().mapToInt(Carteira::getSaldo).sum();
        if (total < vantagem.getCusto()) {
            throw ApiException.saldoParaResgate(total);
        }

        Troca troca = trocas.save(new Troca(agora(), vantagem.getCusto(), aluno, vantagem));
        int falta = vantagem.getCusto();
        doAluno.sort(Comparator.comparingInt(Carteira::getSaldo).reversed()
                .thenComparing(c -> c.getProfessor().getNome()));
        for (Carteira carteira : doAluno) {
            int parte = Math.min(falta, carteira.getSaldo());
            if (parte == 0) {
                continue;
            }
            carteira.debitar(parte);
            debitos.save(new TrocaDebito(troca, carteira.getProfessor(), parte));
            falta -= parte;
            if (falta == 0) {
                break;
            }
        }

        String codigo;
        do {
            codigo = CodigoCupom.novo();
        } while (cupons.existsById(codigo));
        Cupom cupom = cupons.save(new Cupom(codigo, troca));
        CupomResponse resposta = CupomResponse.de(cupom);
        eventos.publishEvent(new CupomNotificado("EMITIDO", resposta, List.of(
                new Aviso(Perfil.ALUNO, idAluno, total - vantagem.getCusto()),
                new Aviso(Perfil.EMPRESA, vantagem.getEmpresa().getId(), 0))));
        return resposta;
    }

    @Transactional(readOnly = true)
    public List<CupomResponse> doAluno(UUID aluno) {
        return cupons.findByTrocaAlunoIdOrderByTrocaDataDesc(aluno).stream().map(CupomResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public List<CupomResponse> daEmpresa(UUID empresa) {
        return cupons.findByTrocaVantagemEmpresaIdOrderByTrocaDataDesc(empresa).stream().map(CupomResponse::de).toList();
    }

    /** Cupom de outra empresa responde como se não existisse: a empresa só confere os das vantagens dela. */
    @Transactional(readOnly = true)
    public CupomResponse buscar(UUID empresa, String digitado) {
        return cupons.findWithTrocaByCodigo(codigo(digitado))
                .filter(c -> c.getTroca().getVantagem().getEmpresa().getId().equals(empresa))
                .map(CupomResponse::de)
                .orElseThrow(ApiException::cupomNaoEncontrado);
    }

    @Transactional
    public CupomResponse confirmar(UUID empresa, String digitado) {
        Cupom cupom = cupons.findBloqueadoByCodigo(codigo(digitado))
                .filter(c -> c.getTroca().getVantagem().getEmpresa().getId().equals(empresa))
                .orElseThrow(ApiException::cupomNaoEncontrado);
        if (cupom.getStatus() == StatusCupom.USADO) {
            throw ApiException.cupomUsado();
        }
        cupom.usar(agora());
        CupomResponse resposta = CupomResponse.de(cupom);
        UUID aluno = cupom.getTroca().getAluno().getId();
        eventos.publishEvent(new CupomNotificado("USADO", resposta,
                List.of(new Aviso(Perfil.ALUNO, aluno, carteiras.somaPorAluno(aluno)))));
        return resposta;
    }

    private static String codigo(String digitado) {
        String codigo = CodigoCupom.normalizar(digitado);
        if (codigo == null) {
            throw ApiException.cupomNaoEncontrado();
        }
        return codigo;
    }

    private Instant agora() {
        return relogio.instant().truncatedTo(ChronoUnit.MICROS);
    }

}
