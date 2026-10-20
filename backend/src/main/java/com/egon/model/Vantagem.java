package com.egon.model;

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

/** Produto, serviço ou desconto que uma empresa parceira oferece em troca de moedas (HU08). */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "vantagem")
public class Vantagem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_vantagem")
    private Integer id;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(nullable = false, length = 1000)
    private String descricao;

    /** Nome do arquivo da foto na pasta de fotos (egon.fotos.pasta). */
    @Column(nullable = false, length = 255)
    private String foto;

    @Column(nullable = false)
    private int custo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TipoVantagem tipo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cnpj_empresa", nullable = false)
    private Empresa empresa;

}
