package com.egon.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.egon.model.Vantagem;

public interface VantagemRepository extends JpaRepository<Vantagem, UUID> {

    List<Vantagem> findByEmpresaIdOrderByNomeAsc(UUID empresa);

    /** O catálogo dos alunos (HU09): do mais barato ao mais caro, com o nome da empresa na mesma consulta. */
    @EntityGraph(attributePaths = "empresa")
    List<Vantagem> findAllByOrderByCustoAscNomeAsc();

}
