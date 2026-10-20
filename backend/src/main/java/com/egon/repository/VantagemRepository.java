package com.egon.repository;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.egon.model.Vantagem;

public interface VantagemRepository extends JpaRepository<Vantagem, Integer> {

    /** O catálogo mostra a empresa de cada vantagem (HU09). */
    @EntityGraph(attributePaths = {"empresa"})
    List<Vantagem> findAllByOrderByCustoAscNomeAsc();

    @EntityGraph(attributePaths = {"empresa"})
    List<Vantagem> findByEmpresaCnpjOrderByNomeAsc(String cnpjEmpresa);

    List<Vantagem> findByEmpresaCnpj(String cnpjEmpresa);

}
