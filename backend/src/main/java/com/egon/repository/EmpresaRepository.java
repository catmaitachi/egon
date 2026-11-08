package com.egon.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.egon.model.Empresa;

public interface EmpresaRepository extends JpaRepository<Empresa, UUID> {

    List<Empresa> findAllByOrderByNomeAsc();

    boolean existsByCnpj(String cnpj);

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, UUID id);

    boolean existsByLogin(String login);

    Optional<Empresa> findByLogin(String login);

}
