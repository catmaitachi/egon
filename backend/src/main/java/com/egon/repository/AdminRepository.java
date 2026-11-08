package com.egon.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.egon.model.Admin;

public interface AdminRepository extends JpaRepository<Admin, UUID> {

    List<Admin> findAllByOrderByNomeAsc();

    Optional<Admin> findByLogin(String login);

    boolean existsByLogin(String login);

}
