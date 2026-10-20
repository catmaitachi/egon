package com.egon.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.egon.model.Transacao;

public interface TransacaoRepository extends JpaRepository<Transacao, UUID> {

    /** Extrato do professor (HU12): o nome de cada aluno vem na mesma consulta. */
    @EntityGraph(attributePaths = {"aluno"})
    List<Transacao> findByProfessorCpf(String cpfProfessor);

    /** Extrato do aluno (HU11): o nome de cada professor vem na mesma consulta. */
    @EntityGraph(attributePaths = {"professor"})
    List<Transacao> findByAlunoCpf(String cpfAluno);

    boolean existsByAlunoCpf(String cpfAluno);

}
