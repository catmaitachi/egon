package com.egon.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.egon.model.Empresa;

public interface EmpresaRepository extends JpaRepository<Empresa, String> {

    List<Empresa> findAllByOrderByNomeAsc();

    boolean existsByEmail(String email);

    boolean existsByEmailAndCnpjNot(String email, String cnpj);

    boolean existsByLogin(String login);

}
