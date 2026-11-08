package com.egon.security;

import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.egon.repository.AdminRepository;
import com.egon.repository.AlunoRepository;
import com.egon.repository.EmpresaRepository;
import com.egon.repository.ProfessorRepository;

import lombok.RequiredArgsConstructor;

/** Acha a conta pelo login nas quatro tabelas de perfil: o login é único entre elas. */
@Service
@RequiredArgsConstructor
public class ContasService implements UserDetailsService {

    private final AlunoRepository alunos;
    private final ProfessorRepository professores;
    private final EmpresaRepository empresas;
    private final AdminRepository admins;

    /** Minúsculo e sem espaço em volta, como o cadastro guarda. */
    @Override
    @Transactional(readOnly = true)
    public ContaLogada loadUserByUsername(String login) {
        String chave = login == null ? "" : login.strip().toLowerCase(Locale.ROOT);
        return alunos.findByLogin(chave)
                .map(a -> new ContaLogada(a.getId(), Perfil.ALUNO, a.getNome(), a.getLogin(), a.getSenha()))
                .or(() -> professores.findByLogin(chave)
                        .map(p -> new ContaLogada(p.getId(), Perfil.PROFESSOR, p.getNome(), p.getLogin(), p.getSenha())))
                .or(() -> empresas.findByLogin(chave)
                        .map(e -> new ContaLogada(e.getId(), Perfil.EMPRESA, e.getNome(), e.getLogin(), e.getSenha())))
                .or(() -> admins.findByLogin(chave)
                        .map(a -> new ContaLogada(a.getId(), Perfil.ADMIN, a.getNome(), a.getLogin(), a.getSenha())))
                .orElseThrow(() -> new UsernameNotFoundException("Login não encontrado."));
    }

    /** Para o modo dev: a conta de um perfil pelo id, sem senha. */
    @Transactional(readOnly = true)
    public Optional<ContaLogada> porId(Perfil perfil, UUID id) {
        return switch (perfil) {
            case ALUNO -> alunos.findById(id).map(a -> new ContaLogada(a.getId(), Perfil.ALUNO, a.getNome(), a.getLogin(), null));
            case PROFESSOR -> professores.findById(id)
                    .map(p -> new ContaLogada(p.getId(), Perfil.PROFESSOR, p.getNome(), p.getLogin(), null));
            case EMPRESA -> empresas.findById(id).map(e -> new ContaLogada(e.getId(), Perfil.EMPRESA, e.getNome(), e.getLogin(), null));
            case ADMIN -> admins.findById(id).map(a -> new ContaLogada(a.getId(), Perfil.ADMIN, a.getNome(), a.getLogin(), null));
        };
    }

    /** A conta da sessão ainda existe? Excluída pelo admin, a sessão não vale mais. */
    @Transactional(readOnly = true)
    public boolean existe(ContaLogada conta) {
        return switch (conta.getPerfil()) {
            case ALUNO -> alunos.existsById(conta.getId());
            case PROFESSOR -> professores.existsById(conta.getId());
            case EMPRESA -> empresas.existsById(conta.getId());
            case ADMIN -> admins.existsById(conta.getId());
        };
    }

}
