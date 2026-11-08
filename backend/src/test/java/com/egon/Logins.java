package com.egon;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import com.egon.repository.AdminRepository;
import com.egon.security.ContaLogada;
import com.egon.security.Perfil;

/**
 * Quem faz a requisição nos testes de MockMvc: a conta logada e o token de CSRF. A conta
 * precisa existir no banco (o filtro de conta ativa confere), por isso o admin é buscado.
 */
@Component
public final class Logins {

    private static AdminRepository admins;

    @Autowired
    void admins(AdminRepository repositorio) {
        admins = repositorio;
    }

    /** Sem login, só com o token de CSRF: os cadastros públicos. */
    public static RequestPostProcessor anonimo() {
        return csrf();
    }

    public static RequestPostProcessor comoAdmin() {
        UUID id = admins.findByLogin("admin").orElseThrow().getId();
        return como(Perfil.ADMIN, id);
    }

    public static RequestPostProcessor comoAluno(UUID id) {
        return como(Perfil.ALUNO, id);
    }

    public static RequestPostProcessor comoProfessor(UUID id) {
        return como(Perfil.PROFESSOR, id);
    }

    public static RequestPostProcessor comoEmpresa(UUID id) {
        return como(Perfil.EMPRESA, id);
    }

    private static RequestPostProcessor como(Perfil perfil, UUID id) {
        RequestPostProcessor conta = user(new ContaLogada(id, perfil, "Teste", "teste", "x"));
        RequestPostProcessor token = csrf();
        return requisicao -> token.postProcessRequest(conta.postProcessRequest(requisicao));
    }

}
