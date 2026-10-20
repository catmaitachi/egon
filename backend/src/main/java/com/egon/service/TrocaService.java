package com.egon.service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.egon.auth.Perfil;
import com.egon.auth.UsuarioLogado;
import com.egon.dto.ResgateResponse;
import com.egon.dto.TrocaRequest;
import com.egon.dto.TrocaResponse;
import com.egon.exception.ApiException;
import com.egon.model.Aluno;
import com.egon.model.Cupom;
import com.egon.model.Troca;
import com.egon.model.Vantagem;
import com.egon.repository.TrocaRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TrocaService {

    private final AlunoService alunos;
    private final VantagemService vantagens;
    private final CupomService cupons;
    private final NotificacaoService notificacoes;
    private final TrocaRepository trocas;
    private final Clock relogio;

    /**
     * HU09 e HU10: confere o saldo com a linha do aluno travada, debita, registra a troca, gera
     * o cupom e avisa aluno e empresa por e-mail.
     */
    @Transactional
    public TrocaResponse resgatar(UsuarioLogado usuario, TrocaRequest requisicao) {
        String cpf = usuario.exigir(Perfil.ALUNO);
        Vantagem vantagem = vantagens.buscar(requisicao.idVantagem())
                .orElseThrow(ApiException::vantagemNaoEncontrada);
        Aluno aluno = alunos.travar(cpf).orElseThrow(ApiException::naoAutenticado);
        if (aluno.getSaldo() < vantagem.getCusto()) {
            throw ApiException.saldoInsuficiente(null, "Saldo insuficiente: esta vantagem custa %s e você tem %s.",
                    vantagem.getCusto(), aluno.getSaldo());
        }

        aluno.setSaldo(aluno.getSaldo() - vantagem.getCusto());
        Troca troca = trocas.save(new Troca(aluno, vantagem, LocalDateTime.now(relogio)));
        Cupom cupom = cupons.gerar(troca);
        notificacoes.trocaRealizada(troca, cupom);

        return new TrocaResponse(troca.getId(), troca.getData(), troca.getValor(), cupom.getCodigo(),
                vantagem.getNome(), vantagem.getEmpresa().getNome(), aluno.getSaldo());
    }

    /** Resgates das vantagens da empresa logada, para conferir o código na troca presencial (HU10). */
    @Transactional(readOnly = true)
    public List<ResgateResponse> resgates(UsuarioLogado usuario) {
        return trocas.daEmpresa(usuario.exigir(Perfil.EMPRESA)).stream().map(ResgateResponse::de).toList();
    }

}
