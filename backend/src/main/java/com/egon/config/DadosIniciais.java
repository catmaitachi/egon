package com.egon.config;

import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.egon.model.Admin;
import com.egon.model.Curso;
import com.egon.model.Instituicao;
import com.egon.model.Professor;
import com.egon.repository.AdminRepository;
import com.egon.repository.CursoRepository;
import com.egon.repository.InstituicaoRepository;
import com.egon.repository.ProfessorRepository;
import com.egon.repository.UsuarioRepository;
import com.egon.service.CotaSemestralService;

import lombok.RequiredArgsConstructor;

/**
 * Instituições, cursos e professores pré-cadastrados, e a cota do semestre atual.
 *
 * <p>Instituições conveniadas e seus professores são pré-cadastrados, nunca criados pela API
 * (HU04). Estes são fictícios, com CNPJs e CPFs de dígitos válidos, até a lista real ser
 * fornecida. Só entram com a tabela vazia, então reiniciar a aplicação não duplica nada.
 *
 * <p>Roda antes de o servidor web aceitar conexões, e não num {@code ApplicationRunner}, que
 * roda depois: na subida, o formulário que abrisse nesse intervalo receberia listas vazias e o
 * front-end as guardaria para a sessão inteira. Pelo mesmo motivo a cota sai aqui: ninguém vê
 * um professor com saldo zerado logo depois de subir.
 */
@Component
@RequiredArgsConstructor
class DadosIniciais implements SmartInitializingSingleton {

    private static final String HORIZONTE = "11222333000181";
    private static final String VALE_VERDE = "45218073000192";
    private static final String AURORA = "28039156000103";

    /** Senha inicial dos professores pré-cadastrados; a troca depende do login (HU01). */
    private static final String SENHA_INICIAL = "professor123";

    private final InstituicaoRepository instituicoes;
    private final CursoRepository cursos;
    private final ProfessorRepository professores;
    private final AdminRepository admins;
    private final UsuarioRepository usuarios;
    private final CotaSemestralService cotas;
    private final PasswordEncoder senhas;

    /** Senha do primeiro admin; em produção, vem de variável de ambiente (EGON_ADMIN_SENHA). */
    @Value("${egon.admin.senha}")
    private String senhaAdmin;

    @Override
    @Transactional
    public void afterSingletonsInstantiated() {
        if (instituicoes.count() == 0) {
            instituicoes.saveAll(List.of(
                    new Instituicao(HORIZONTE, "Universidade Horizonte", "Universidade"),
                    new Instituicao(VALE_VERDE, "Centro Universitário Vale Verde", "Centro Universitário"),
                    new Instituicao(AURORA, "Faculdade de Tecnologia Aurora", "Faculdade")));
        }
        if (cursos.count() == 0) {
            cursos.saveAll(List.of(
                    new Curso("Administração"),
                    new Curso("Arquitetura e Urbanismo"),
                    new Curso("Ciência da Computação"),
                    new Curso("Direito"),
                    new Curso("Engenharia Civil"),
                    new Curso("Engenharia de Software"),
                    new Curso("Psicologia"),
                    new Curso("Sistemas de Informação")));
        }
        if (professores.count() == 0) {
            professores.saveAll(List.of(
                    professor("48291573646", "Helena Duarte", "Departamento de Computação", HORIZONTE),
                    professor("61528394755", "Ricardo Moraes", "Departamento de Engenharia", HORIZONTE),
                    professor("73946125891", "Beatriz Campos", "Departamento de Psicologia", VALE_VERDE),
                    professor("85713462919", "Marcelo Tavares", "Departamento de Gestão", AURORA)));
        }
        if (admins.count() == 0) {
            exigirLoginLivre("admin", "o admin inicial");
            Admin admin = new Admin();
            admin.setNome("Administrador");
            admin.setLogin("admin");
            admin.setSenha(senhas.encode(senhaAdmin));
            admins.save(admin);
        }
        cotas.creditar();
    }

    /** O banco só garante login único dentro de cada tabela, e a carga pode subir sobre cadastros antigos. */
    private void exigirLoginLivre(String login, String quem) {
        if (usuarios.existsByLogin(login)) {
            throw new IllegalStateException("O login " + login + ", de " + quem
                    + ", já é de outra conta. Troque esse login antes de subir a aplicação.");
        }
    }

    private Professor professor(String cpf, String nome, String departamento, String cnpjInstituicao) {
        String login = nome.toLowerCase(Locale.ROOT).replace(' ', '.');
        exigirLoginLivre(login, "o professor pré-cadastrado " + nome);
        Professor professor = new Professor();
        professor.setCpf(cpf);
        professor.setNome(nome);
        professor.setDepartamento(departamento);
        professor.setInstituicao(instituicoes.findByCnpj(cnpjInstituicao).orElseThrow());
        professor.setLogin(login);
        professor.setSenha(senhas.encode(SENHA_INICIAL));
        return professor;
    }

}
