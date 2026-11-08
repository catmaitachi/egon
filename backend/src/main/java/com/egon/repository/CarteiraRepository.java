package com.egon.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import com.egon.model.Carteira;

import jakarta.persistence.LockModeType;

public interface CarteiraRepository extends JpaRepository<Carteira, UUID> {

    /** Para mexer no saldo. Quem chama já travou o professor da carteira antes. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Carteira> findBloqueadaByAlunoIdAndProfessorId(UUID aluno, UUID professor);

    @EntityGraph(attributePaths = "professor")
    List<Carteira> findByAlunoIdOrderByProfessorNomeAsc(UUID aluno);

    /** Só os ids dos professores: o resgate trava cada um antes de ler as carteiras com trava. */
    @Query("select c.professor.id from Carteira c where c.aluno.id = :aluno")
    List<UUID> professoresDoAluno(UUID aluno);

    /** O saldo total de cada aluno que tem alguma carteira (para contar quem pode pagar uma vantagem). */
    @Query("select sum(c.saldo) from Carteira c group by c.aluno.id")
    List<Long> saldosPorAluno();

    @Query("select coalesce(sum(c.saldo), 0) from Carteira c")
    long somaTotal();

    @Query("select coalesce(sum(c.saldo), 0) from Carteira c where c.aluno.id = :aluno")
    int somaPorAluno(UUID aluno);

}
