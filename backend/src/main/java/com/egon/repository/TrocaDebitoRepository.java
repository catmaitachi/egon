package com.egon.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.egon.model.TrocaDebito;

public interface TrocaDebitoRepository extends JpaRepository<TrocaDebito, UUID> {
}
