package com.egon.repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.egon.model.Transacao;

public interface TransacaoRepository extends JpaRepository<Transacao, UUID> {

    /** O nome de quem enviou aparece em toda linha do extrato do aluno: vem na mesma consulta. */
    @EntityGraph(attributePaths = "professor")
    List<Transacao> findByAlunoIdOrderByDataDesc(UUID id);

    @EntityGraph(attributePaths = "aluno")
    List<Transacao> findByProfessorIdOrderByDataDesc(UUID id);

    boolean existsByAlunoId(UUID id);

    boolean existsByProfessorId(UUID id);

    List<Transacao> findByDataGreaterThanEqual(Instant desde);

    /** Pares (id da instituição do professor, Egos enviados). */
    @Query("select t.professor.instituicao.id, sum(t.valor) from Transacao t group by t.professor.instituicao.id")
    List<Object[]> enviadoPorInstituicao();

}
