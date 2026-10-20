package com.egon.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.egon.model.Curso;

public interface CursoRepository extends JpaRepository<Curso, Integer> {

    List<Curso> findAllByOrderByNomeAsc();

}
