package com.egon.service;

import java.util.List;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.egon.dto.AdminAtualizacaoRequest;
import com.egon.dto.AdminCadastroRequest;
import com.egon.dto.AdminResponse;
import com.egon.exception.ApiException;
import com.egon.model.Admin;
import com.egon.repository.AdminRepository;
import com.egon.repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final AdminRepository admins;
    private final UsuarioRepository usuarios;
    private final PasswordEncoder senhas;

    @Transactional(readOnly = true)
    public List<AdminResponse> listar() {
        return admins.findAllByOrderByNomeAsc().stream().map(AdminResponse::de).toList();
    }

    @Transactional
    public AdminResponse cadastrar(AdminCadastroRequest requisicao) {
        if (usuarios.existsByLogin(requisicao.login())) {
            throw ApiException.loginEmUso();
        }
        Admin admin = new Admin();
        admin.setNome(requisicao.nome());
        admin.setLogin(requisicao.login());
        admin.setSenha(senhas.encode(requisicao.senha()));
        return AdminResponse.de(admins.save(admin));
    }

    @Transactional
    public AdminResponse atualizar(UUID id, AdminAtualizacaoRequest requisicao) {
        Admin admin = encontrar(id);
        if (!requisicao.login().equals(admin.getLogin()) && usuarios.existsByLogin(requisicao.login())) {
            throw ApiException.loginEmUso();
        }
        admin.setNome(requisicao.nome());
        admin.setLogin(requisicao.login());
        if (requisicao.senha() != null) {
            admin.setSenha(senhas.encode(requisicao.senha()));
        }
        return AdminResponse.de(admin);
    }

    /** Sempre sobra alguém para administrar: ninguém exclui a si mesmo nem o último admin. */
    @Transactional
    public void excluir(UUID id, UUID quemPede) {
        Admin admin = encontrar(id);
        if (id.equals(quemPede) || admins.count() <= 1) {
            throw ApiException.adminProtegido();
        }
        admins.delete(admin);
    }

    private Admin encontrar(UUID id) {
        return admins.findById(id).orElseThrow(ApiException::adminNaoEncontrado);
    }

}
