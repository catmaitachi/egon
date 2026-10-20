package com.egon.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import com.egon.model.Professor;

import jakarta.persistence.LockModeType;

public interface ProfessorRepository extends JpaRepository<Professor, String> {

    @EntityGraph(attributePaths = {"instituicao", "cursos"})
    Optional<Professor> findWithReferenciasByCpf(String cpf);

    Optional<Professor> findByLogin(String login);

    boolean existsByLogin(String login);

    /** Trava a linha do professor até o fim da transação, para dois envios simultâneos não gastarem o mesmo saldo. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Professor p join fetch p.instituicao where p.cpf = :cpf")
    Optional<Professor> travar(String cpf);

}
