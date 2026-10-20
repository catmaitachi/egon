package com.egon.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.egon.model.Instituicao;

public interface InstituicaoRepository extends JpaRepository<Instituicao, String> {

    List<Instituicao> findAllByOrderByNomeAsc();

}
