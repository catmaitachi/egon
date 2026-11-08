package com.egon.model;

import org.hibernate.annotations.ColumnDefault;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * O que o professor permite fazer com os Egos que deu, no repasse entre alunos. Valem no momento
 * do pedido; mudar uma regra não mexe nas transferências pendentes. Os defaults no banco deixam o
 * {@code ddl-auto=update} criar as colunas numa tabela que já tem professores.
 */
@Getter
@Setter
@NoArgsConstructor
@Embeddable
public class RegrasMoeda {

    @Column(name = "regra_permite_repasse", nullable = false)
    @ColumnDefault("true")
    private boolean permiteRepasse = true;

    @Column(name = "regra_exige_aprovacao", nullable = false)
    @ColumnDefault("true")
    private boolean exigeAprovacao = true;

    /** Máximo por transferência; nulo é sem limite. */
    @Column(name = "regra_valor_maximo")
    private Integer valorMaximo;

    /** Máximo que cada aluno repassa por mês dos Egos deste professor; nulo é sem limite. */
    @Column(name = "regra_limite_mensal")
    private Integer limiteMensal;

    /** Só para colegas do mesmo curso do remetente. */
    @Column(name = "regra_mesmo_curso", nullable = false)
    @ColumnDefault("false")
    private boolean mesmoCurso;

}
