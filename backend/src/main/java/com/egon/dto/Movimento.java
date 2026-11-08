package com.egon.dto;

import java.time.Instant;

import com.egon.model.CotaSemestral;
import com.egon.model.Cupom;
import com.egon.model.StatusTransferencia;
import com.egon.model.Troca;
import com.egon.model.Transacao;
import com.egon.model.Transferencia;

/**
 * Uma linha do extrato. O valor é sempre positivo: entrada ou saída vem do tipo. Os campos
 * que não se aplicam ao tipo vêm nulos (a cota não tem motivo nem outra pessoa; só repasse tem status e
 * origem, o professor de quem são os Egos).
 */
public record Movimento(
        String tipo,
        String id,
        Instant data,
        int valor,
        String motivo,
        PessoaResponse pessoa,
        String semestre,
        StatusTransferencia status,
        PessoaResponse origem,
        String cupom) {

    /** No extrato do aluno: a outra pessoa é o professor. */
    public static Movimento recebimento(Transacao transacao) {
        return new Movimento("RECEBIMENTO", transacao.getId().toString(), transacao.getData(), transacao.getValor(),
                transacao.getMotivo(), PessoaResponse.de(transacao.getProfessor()), null, null, null, null);
    }

    /** No extrato do professor: a outra pessoa é o aluno. */
    public static Movimento envio(Transacao transacao) {
        return new Movimento("ENVIO", transacao.getId().toString(), transacao.getData(), transacao.getValor(),
                transacao.getMotivo(), PessoaResponse.de(transacao.getAluno()), null, null, null, null);
    }

    public static Movimento cota(CotaSemestral cota) {
        return new Movimento("COTA", cota.getId().toString(), cota.getData(), cota.getValor(),
                null, null, cota.getSemestre(), null, null, null);
    }

    /** No extrato de quem repassou: a outra pessoa é o colega; vale em qualquer status. */
    public static Movimento repasseEnviado(Transferencia t) {
        return new Movimento("TRANSFERENCIA_ENVIADA", t.getId().toString(), t.getData(), t.getValor(), t.getMotivo(),
                PessoaResponse.de(t.getDestinatario()), null, t.getStatus(), PessoaResponse.de(t.getProfessor()), null);
    }

    /** No extrato de quem recebeu (só aprovadas): a outra pessoa é quem repassou. */
    public static Movimento repasseRecebido(Transferencia t) {
        return new Movimento("TRANSFERENCIA_RECEBIDA", t.getId().toString(), t.getData(), t.getValor(), t.getMotivo(),
                PessoaResponse.de(t.getRemetente()), null, t.getStatus(), PessoaResponse.de(t.getProfessor()), null);
    }

    /** No extrato do aluno: o resgate é saída; o motivo é a vantagem, a pessoa é a empresa, e vem o código do cupom. */
    public static Movimento resgate(Cupom cupom) {
        Troca troca = cupom.getTroca();
        return new Movimento("RESGATE", troca.getId().toString(), troca.getData(), troca.getValor(),
                troca.getVantagem().getNome(), PessoaResponse.de(troca.getVantagem().getEmpresa()), null, null, null,
                cupom.getCodigo());
    }

}
