package com.egon.model;

import java.util.UUID;

import org.hibernate.annotations.Check;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Tabela VANTAGEM (HU08): o que uma empresa parceira oferece em troca de Egos. A foto fica numa
 * pasta em disco; aqui só o nome do arquivo.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "vantagem")
public class Vantagem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(nullable = false, length = 1000)
    private String descricao;

    /** Nome do arquivo na pasta de fotos, gerado pelo sistema (nunca o nome enviado). */
    @Column(nullable = false, length = 100)
    private String foto;

    @Column(nullable = false)
    @Check(constraints = "custo > 0")
    private int custo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TipoVantagem tipo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

}
