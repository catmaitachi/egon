package com.egon.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.egon.model.CotaSemestral;

public interface CotaSemestralRepository extends JpaRepository<CotaSemestral, Long> {

    List<CotaSemestral> findByProfessorIdOrderByDataDesc(UUID id);

    boolean existsByProfessorIdAndSemestre(UUID id, String semestre);

    void deleteByProfessorId(UUID id);

    /** Todos os Egos que já entraram no sistema: só a cota cria Ego. */
    @Query("select coalesce(sum(c.valor), 0) from CotaSemestral c")
    long somaEmitida();

}
