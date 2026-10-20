package com.egon.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.egon.model.Troca;

public interface TrocaRepository extends JpaRepository<Troca, UUID> {

    /** Trocas do aluno, com vantagem, empresa e cupom, para o extrato (HU11). */
    @Query("""
            select t from Troca t
            join fetch t.vantagem v join fetch v.empresa left join fetch t.cupom
            where t.aluno.cpf = :cpfAluno
            """)
    List<Troca> doAluno(String cpfAluno);

    /** Resgates das vantagens de uma empresa, com o aluno, para a conferência do cupom (HU10). */
    @Query("""
            select t from Troca t
            join fetch t.vantagem v join fetch t.aluno a join fetch a.instituicao join fetch a.curso
            left join fetch t.cupom
            where v.empresa.cnpj = :cnpjEmpresa
            order by t.data desc
            """)
    List<Troca> daEmpresa(String cnpjEmpresa);

    boolean existsByAlunoCpf(String cpfAluno);

    boolean existsByVantagemId(Integer idVantagem);

    boolean existsByVantagemEmpresaCnpj(String cnpjEmpresa);

}
