package com.egon.service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.egon.auth.Perfil;
import com.egon.auth.UsuarioLogado;
import com.egon.dto.AlunoResumoResponse;
import com.egon.dto.ExtratoResponse;
import com.egon.dto.LancamentoResponse;
import com.egon.dto.ProfessorResponse;
import com.egon.exception.ApiException;
import com.egon.model.CotaSemestral;
import com.egon.model.Professor;
import com.egon.repository.AlunoRepository;
import com.egon.repository.CotaSemestralRepository;
import com.egon.repository.ProfessorRepository;
import com.egon.repository.TransacaoRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProfessorService {

    /** HU05: o crédito de cada semestre. */
    public static final int COTA_SEMESTRAL = 1000;

    private final ProfessorRepository professores;
    private final CotaSemestralRepository cotas;
    private final TransacaoRepository transacoes;
    private final AlunoRepository alunos;
    private final Clock relogio;

    @Transactional(readOnly = true)
    public ProfessorResponse buscar(UsuarioLogado usuario) {
        return professores.findWithReferenciasByCpf(usuario.exigir(Perfil.PROFESSOR))
                .map(ProfessorResponse::de)
                .orElseThrow(ApiException::naoAutenticado);
    }

    /** HU06: o professor pesquisa o destinatário entre os alunos da instituição dele. */
    @Transactional(readOnly = true)
    public List<AlunoResumoResponse> alunosDaInstituicao(UsuarioLogado usuario, String busca) {
        Professor professor = professores.findWithReferenciasByCpf(usuario.exigir(Perfil.PROFESSOR))
                .orElseThrow(ApiException::naoAutenticado);
        String termo = busca == null ? "" : busca.strip();
        return alunos.findByInstituicaoCnpjAndNomeContainingIgnoreCaseOrderByNomeAsc(
                        professor.getInstituicao().getCnpj(), termo)
                .stream().map(AlunoResumoResponse::de).toList();
    }

    /** HU12: saldo para distribuir, os envios feitos e as cotas recebidas (HU05). */
    @Transactional(readOnly = true)
    public ExtratoResponse extrato(UsuarioLogado usuario) {
        Professor professor = professores.findById(usuario.exigir(Perfil.PROFESSOR))
                .orElseThrow(ApiException::naoAutenticado);
        List<LancamentoResponse> envios = transacoes.findByProfessorCpf(professor.getCpf()).stream()
                .map(t -> LancamentoResponse.transacao("SAIDA", t.getData(), t.getValor(), t.getAluno().getNome(),
                        t.getMotivo()))
                .toList();
        List<LancamentoResponse> creditos = cotas.findByProfessorCpf(professor.getCpf()).stream()
                .map(c -> LancamentoResponse.cota(c.getData(), c.getValor(), c.getSemestre()))
                .toList();
        return ExtratoResponse.de(professor.getSaldo(), envios, creditos);
    }

    /** Professor com a linha travada, para quem vai mexer no saldo dentro da transação corrente. */
    @Transactional(propagation = Propagation.MANDATORY)
    public Optional<Professor> travar(String cpf) {
        return professores.travar(cpf);
    }

    /**
     * HU05: soma a cota do semestre ao saldo de cada professor que ainda não a recebeu. O saldo
     * que sobrou do semestre anterior continua lá. Rodar de novo no mesmo semestre não credita
     * nada: cada crédito fica em COTA_SEMESTRAL, com chave única por professor e semestre.
     *
     * @return quantos professores foram creditados
     */
    @Transactional
    public int creditarCotaSemestral(String semestre) {
        LocalDateTime agora = LocalDateTime.now(relogio);
        List<Professor> pendentes = professores.findAll().stream()
                .filter(p -> !cotas.existsByProfessorCpfAndSemestre(p.getCpf(), semestre))
                .flatMap(p -> professores.travar(p.getCpf()).stream())
                .toList();
        pendentes.forEach(professor -> {
            professor.setSaldo(professor.getSaldo() + COTA_SEMESTRAL);
            cotas.save(new CotaSemestral(professor, semestre, COTA_SEMESTRAL, agora));
        });
        return pendentes.size();
    }

}
