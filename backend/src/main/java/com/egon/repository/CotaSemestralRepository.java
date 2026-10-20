package com.egon.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.egon.model.CotaSemestral;

public interface CotaSemestralRepository extends JpaRepository<CotaSemestral, Integer> {

    boolean existsByProfessorCpfAndSemestre(String cpfProfessor, String semestre);

    List<CotaSemestral> findByProfessorCpf(String cpfProfessor);

}
