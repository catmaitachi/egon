package com.egon.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import com.egon.model.Aluno;

import jakarta.persistence.LockModeType;

public interface AlunoRepository extends JpaRepository<Aluno, UUID> {

    /** Instituição e curso aparecem em toda linha da listagem: vêm na mesma consulta. */
    @EntityGraph(attributePaths = {"instituicao", "curso"})
    List<Aluno> findAllByOrderByNomeAsc();

    @EntityGraph(attributePaths = {"instituicao", "curso"})
    Optional<Aluno> findWithReferenciasById(UUID id);

    /**
     * Segura a linha até o fim da transação: um envio e a exclusão ou edição do mesmo aluno
     * esperam um pelo outro, e a exclusão não esbarra numa transação gravada no meio.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Aluno> findBloqueadoById(UUID id);

    boolean existsByCpf(String cpf);

    /** Pares (id da instituição, quantidade de alunos). */
    @Query("select a.instituicao.id, count(a) from Aluno a group by a.instituicao.id")
    List<Object[]> contarPorInstituicao();

    /** Para escolher destinatário: os alunos de uma instituição, com o curso na mesma consulta. */
    @EntityGraph(attributePaths = "curso")
    List<Aluno> findByInstituicaoIdOrderByNomeAsc(UUID instituicao);

    boolean existsByInstituicaoId(UUID id);

    boolean existsByCursoId(Integer id);

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, UUID id);

    boolean existsByLogin(String login);

    Optional<Aluno> findByLogin(String login);

}
