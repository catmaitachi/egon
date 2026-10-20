package com.egon.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
 * Registro de cada e-mail que o sistema manda (HU07, HU10). O destinatário é um aluno ou uma
 * empresa; o cupom aparece só nos e-mails de troca.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "notificacao")
public class Notificacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_notificacao")
    private Integer id;

    @Column(nullable = false, length = 150)
    private String assunto;

    /** Corpo do e-mail. */
    @Column(nullable = false, length = 2000)
    private String descricao;

    @Column(nullable = false)
    private LocalDateTime data;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "codigo_cupom")
    private Cupom cupom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cpf_aluno")
    private Aluno aluno;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cnpj_empresa")
    private Empresa empresa;

}
