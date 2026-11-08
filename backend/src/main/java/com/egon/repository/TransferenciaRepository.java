package com.egon.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import com.egon.model.Transferencia;

import jakarta.persistence.LockModeType;

public interface TransferenciaRepository extends JpaRepository<Transferencia, UUID> {

    /** Só o professor de origem: com ele em mãos, a decisão trava o professor antes da transferência. */
    @Query("select t.professor.id from Transferencia t where t.id = :id")
    Optional<UUID> findProfessorIdById(UUID id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Transferencia> findBloqueadaById(UUID id);

    @EntityGraph(attributePaths = {"remetente", "destinatario", "professor"})
    List<Transferencia> findByRemetenteIdOrDestinatarioIdOrderByDataDesc(UUID remetente, UUID destinatario);

    @EntityGraph(attributePaths = {"remetente", "destinatario", "professor"})
    List<Transferencia> findByProfessorIdOrderByDataDesc(UUID professor);

    /** O que o aluno já repassou dos Egos deste professor no mês: pendentes e aprovadas contam. */
    @Query("""
            select coalesce(sum(t.valor), 0) from Transferencia t
            where t.remetente.id = :aluno and t.professor.id = :professor and t.data >= :desde
              and t.status in (com.egon.model.StatusTransferencia.PENDENTE, com.egon.model.StatusTransferencia.APROVADA)
            """)
    int somaNoMes(UUID aluno, UUID professor, Instant desde);

    /** Egos reservados em transferências que esperam aprovação. */
    @Query("select coalesce(sum(t.valor), 0) from Transferencia t where t.status = com.egon.model.StatusTransferencia.PENDENTE")
    long somaPendente();

    boolean existsByRemetenteIdOrDestinatarioId(UUID remetente, UUID destinatario);

}
