package com.egon.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import com.egon.model.Aluno;

import jakarta.persistence.LockModeType;

public interface AlunoRepository extends JpaRepository<Aluno, String> {

    /** Instituição e curso aparecem em toda linha da listagem: vêm na mesma consulta. */
    @EntityGraph(attributePaths = {"instituicao", "curso"})
    List<Aluno> findAllByOrderByNomeAsc();

    @EntityGraph(attributePaths = {"instituicao", "curso"})
    Optional<Aluno> findWithReferenciasByCpf(String cpf);

    /** Os alunos que um professor pode reconhecer: os da instituição dele (HU06). */
    @EntityGraph(attributePaths = {"curso"})
    List<Aluno> findByInstituicaoCnpjAndNomeContainingIgnoreCaseOrderByNomeAsc(String cnpjInstituicao, String nome);

    Optional<Aluno> findByLogin(String login);

    /**
     * Trava a linha do aluno até o fim da transação: duas operações sobre o mesmo saldo (uma
     * transferência recebida e uma troca, por exemplo) esperam uma pela outra em vez de uma
     * apagar o resultado da outra.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from Aluno a join fetch a.instituicao join fetch a.curso where a.cpf = :cpf")
    Optional<Aluno> travar(String cpf);

    boolean existsByEmail(String email);

    boolean existsByEmailAndCpfNot(String email, String cpf);

    boolean existsByLogin(String login);

}
