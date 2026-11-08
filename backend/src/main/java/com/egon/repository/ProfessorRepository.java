package com.egon.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import com.egon.model.Professor;

import jakarta.persistence.LockModeType;

public interface ProfessorRepository extends JpaRepository<Professor, UUID> {

    @EntityGraph(attributePaths = "instituicao")
    List<Professor> findAllByOrderByNomeAsc();

    @EntityGraph(attributePaths = "instituicao")
    Optional<Professor> findWithReferenciasById(UUID id);

    Optional<Professor> findByCpf(String cpf);

    /** Para mexer no saldo: segura a linha até o fim da transação, e quem vier depois lê o saldo já atualizado. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Professor> findBloqueadoById(UUID id);

    /** Só os ids: carregar os professores aqui faria o {@link #findBloqueadoById} devolver a cópia sem trava. */
    @Query("""
            select p.id from Professor p
            where not exists (select 1 from CotaSemestral c where c.professor = p and c.semestre = :semestre)
            """)
    List<UUID> findIdsSemCota(String semestre);

    boolean existsByLogin(String login);

    boolean existsByCpf(String cpf);

    @Query("select coalesce(sum(p.saldo), 0) from Professor p")
    long somaSaldos();

    /** Pares (id da instituição, quantidade de professores). */
    @Query("select p.instituicao.id, count(p) from Professor p group by p.instituicao.id")
    List<Object[]> contarPorInstituicao();

    boolean existsByInstituicaoId(UUID id);

    Optional<Professor> findByLogin(String login);

}
