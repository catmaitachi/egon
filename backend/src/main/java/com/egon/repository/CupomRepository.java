package com.egon.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.egon.model.Cupom;

public interface CupomRepository extends JpaRepository<Cupom, String> {
}
