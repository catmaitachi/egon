package com.egon.model;

import org.springframework.data.domain.Persistable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Código que o aluno apresenta na troca presencial e a empresa confere (HU10).
 *
 * <p>O código é a chave e é gerado pela aplicação, como o CPF do aluno: pelo mesmo motivo de
 * {@link Usuario}, é {@link Persistable}, para um código repetido esbarrar na chave primária em
 * vez de sobrescrever o cupom de outra troca.
 */
@Getter
@NoArgsConstructor
@Entity
@Table(name = "cupom")
public class Cupom implements Persistable<String> {

    @Id
    @Column(length = 20)
    private String codigo;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_troca", nullable = false, unique = true)
    private Troca troca;

    @Transient
    private boolean novo = true;

    public Cupom(String codigo, Troca troca) {
        this.codigo = codigo;
        this.troca = troca;
    }

    @Override
    public String getId() {
        return codigo;
    }

    @Override
    public boolean isNew() {
        return novo;
    }

    @PrePersist
    @PostLoad
    void marcarComoPersistido() {
        novo = false;
    }

}
