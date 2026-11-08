package com.egon.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.egon.model.Troca;

public interface TrocaRepository extends JpaRepository<Troca, UUID> {

    boolean existsByVantagemId(UUID vantagem);

    boolean existsByVantagemEmpresaId(UUID empresa);

    boolean existsByAlunoId(UUID aluno);

    /** Egos que saíram do sistema por resgate. */
    @Query("select coalesce(sum(t.valor), 0) from Troca t")
    long somaResgatada();

}
