package com.egon.service;

import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.egon.auth.Perfil;
import com.egon.auth.TokenService;
import com.egon.auth.UsuarioLogado;
import com.egon.dto.LoginRequest;
import com.egon.dto.SessaoResponse;
import com.egon.exception.ApiException;
import com.egon.model.Aluno;
import com.egon.model.Empresa;
import com.egon.model.Professor;
import com.egon.model.Usuario;
import com.egon.repository.AlunoRepository;
import com.egon.repository.EmpresaRepository;
import com.egon.repository.ProfessorRepository;

/** HU01: login único para aluno, professor e empresa. */
@Service
public class AuthService {

    private final AlunoRepository alunos;
    private final ProfessorRepository professores;
    private final EmpresaRepository empresas;
    private final PasswordEncoder senhas;
    private final TokenService tokens;

    /**
     * Login inexistente também passa por uma comparação de BCrypt: sem ela, a resposta mais
     * rápida denunciaria quais logins existem.
     */
    private final String hashFicticio;

    public AuthService(AlunoRepository alunos, ProfessorRepository professores, EmpresaRepository empresas,
            PasswordEncoder senhas, TokenService tokens) {
        this.alunos = alunos;
        this.professores = professores;
        this.empresas = empresas;
        this.senhas = senhas;
        this.tokens = tokens;
        this.hashFicticio = senhas.encode("login-inexistente");
    }

    @Transactional(readOnly = true)
    public SessaoResponse entrar(LoginRequest requisicao) {
        Optional<Usuario> usuario = alunos.findByLogin(requisicao.login()).<Usuario>map(a -> a)
                .or(() -> professores.findByLogin(requisicao.login()))
                .or(() -> empresas.findByLogin(requisicao.login()));
        String hash = usuario.map(Usuario::getSenha).orElse(hashFicticio);
        if (!senhas.matches(requisicao.senha(), hash) || usuario.isEmpty()) {
            throw ApiException.credenciaisInvalidas();
        }
        UsuarioLogado logado = new UsuarioLogado(perfil(usuario.get()), usuario.get().getId());
        return new SessaoResponse(tokens.emitir(logado), logado.perfil(), logado.id(), usuario.get().getNome());
    }

    /** Confere que a conta do token ainda existe: um cadastro excluído não continua logado. */
    @Transactional(readOnly = true)
    public SessaoResponse sessao(UsuarioLogado logado) {
        Optional<? extends Usuario> usuario = switch (logado.perfil()) {
            case ALUNO -> alunos.findById(logado.id());
            case PROFESSOR -> professores.findById(logado.id());
            case EMPRESA -> empresas.findById(logado.id());
        };
        return usuario.map(u -> new SessaoResponse(null, logado.perfil(), logado.id(), u.getNome()))
                .orElseThrow(ApiException::naoAutenticado);
    }

    private static Perfil perfil(Usuario usuario) {
        return switch (usuario) {
            case Aluno a -> Perfil.ALUNO;
            case Professor p -> Perfil.PROFESSOR;
            case Empresa e -> Perfil.EMPRESA;
            default -> throw new IllegalStateException("Perfil desconhecido: " + usuario.getClass());
        };
    }

}
