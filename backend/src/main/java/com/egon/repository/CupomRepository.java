package com.egon.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import com.egon.model.Cupom;

import jakarta.persistence.LockModeType;

public interface CupomRepository extends JpaRepository<Cupom, String> {

    /** Vantagem, empresa e aluno aparecem em toda linha: vêm na mesma consulta. */
    @EntityGraph(attributePaths = {"troca", "troca.aluno", "troca.vantagem", "troca.vantagem.empresa"})
    List<Cupom> findByTrocaAlunoIdOrderByTrocaDataDesc(UUID aluno);

    @EntityGraph(attributePaths = {"troca", "troca.aluno", "troca.vantagem", "troca.vantagem.empresa"})
    List<Cupom> findByTrocaVantagemEmpresaIdOrderByTrocaDataDesc(UUID empresa);

    @EntityGraph(attributePaths = {"troca", "troca.aluno", "troca.vantagem", "troca.vantagem.empresa"})
    Optional<Cupom> findWithTrocaByCodigo(String codigo);

    /** Para confirmar: duas confirmações ao mesmo tempo esperam uma pela outra e só a primeira vale. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Cupom> findBloqueadoByCodigo(String codigo);

}
