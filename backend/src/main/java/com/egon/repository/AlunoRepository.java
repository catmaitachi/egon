package com.egon.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.egon.model.Aluno;

public interface AlunoRepository extends JpaRepository<Aluno, String> {

    /** Instituição e curso aparecem em toda linha da listagem: vêm na mesma consulta. */
    @EntityGraph(attributePaths = {"instituicao", "curso"})
    List<Aluno> findAllByOrderByNomeAsc();

    @EntityGraph(attributePaths = {"instituicao", "curso"})
    Optional<Aluno> findWithReferenciasByCpf(String cpf);

    boolean existsByEmail(String email);

    boolean existsByEmailAndCpfNot(String email, String cpf);

    boolean existsByLogin(String login);

}
