package com.egon.repository;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.egon.model.Notificacao;

public interface NotificacaoRepository extends JpaRepository<Notificacao, Integer> {

    @EntityGraph(attributePaths = {"cupom"})
    List<Notificacao> findByAlunoCpfOrderByDataDescIdDesc(String cpfAluno);

    @EntityGraph(attributePaths = {"cupom"})
    List<Notificacao> findByEmpresaCnpjOrderByDataDescIdDesc(String cnpjEmpresa);

}
