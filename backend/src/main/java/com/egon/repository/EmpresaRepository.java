package com.egon.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.egon.model.Empresa;

public interface EmpresaRepository extends JpaRepository<Empresa, String> {

    List<Empresa> findAllByOrderByNomeAsc();

    Optional<Empresa> findByLogin(String login);

    boolean existsByEmail(String email);

    boolean existsByEmailAndCnpjNot(String email, String cnpj);

    boolean existsByLogin(String login);

}
