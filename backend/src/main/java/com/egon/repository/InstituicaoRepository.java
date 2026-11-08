package com.egon.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.egon.model.Instituicao;

public interface InstituicaoRepository extends JpaRepository<Instituicao, UUID> {

    List<Instituicao> findAllByOrderByNomeAsc();

    Optional<Instituicao> findByCnpj(String cnpj);

    boolean existsByCnpj(String cnpj);

}
