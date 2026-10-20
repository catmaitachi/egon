package com.egon.service;

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.egon.dto.EmpresaAtualizacaoRequest;
import com.egon.dto.EmpresaCadastroRequest;
import com.egon.dto.EmpresaResponse;
import com.egon.exception.ApiException;
import com.egon.model.Empresa;
import com.egon.repository.EmpresaRepository;
import com.egon.repository.TrocaRepository;
import com.egon.repository.UsuarioRepository;
import com.egon.validation.Documentos;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmpresaService {

    private final EmpresaRepository empresas;
    private final UsuarioRepository usuarios;
    private final VantagemService vantagens;
    private final TrocaRepository trocas;
    private final PasswordEncoder senhas;

    @Transactional(readOnly = true)
    public List<EmpresaResponse> listar() {
        return empresas.findAllByOrderByNomeAsc().stream().map(EmpresaResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public EmpresaResponse buscar(String cnpj) {
        return EmpresaResponse.de(encontrar(cnpj));
    }

    /** HU03: CNPJ repetido impede o cadastro. E-mail e login também precisam ser únicos. */
    @Transactional
    public EmpresaResponse cadastrar(EmpresaCadastroRequest requisicao) {
        if (empresas.existsById(requisicao.cnpj())) {
            throw ApiException.cnpjEmUso();
        }
        if (empresas.existsByEmail(requisicao.email())) {
            throw ApiException.emailEmUso();
        }
        if (usuarios.existsByLogin(requisicao.login())) {
            throw ApiException.loginEmUso();
        }

        Empresa empresa = new Empresa();
        empresa.setCnpj(requisicao.cnpj());
        empresa.setNome(requisicao.nome());
        empresa.setEmail(requisicao.email());
        empresa.setLogin(requisicao.login());
        empresa.setSenha(senhas.encode(requisicao.senha()));
        return EmpresaResponse.de(empresas.save(empresa));
    }

    @Transactional
    public EmpresaResponse atualizar(String cnpj, EmpresaAtualizacaoRequest requisicao) {
        Empresa empresa = encontrar(cnpj);

        if (empresas.existsByEmailAndCnpjNot(requisicao.email(), empresa.getCnpj())) {
            throw ApiException.emailEmUso();
        }
        if (!requisicao.login().equals(empresa.getLogin()) && usuarios.existsByLogin(requisicao.login())) {
            throw ApiException.loginEmUso();
        }

        empresa.setNome(requisicao.nome());
        empresa.setEmail(requisicao.email());
        empresa.setLogin(requisicao.login());
        if (requisicao.senha() != null) {
            empresa.setSenha(senhas.encode(requisicao.senha()));
        }
        return EmpresaResponse.de(empresa);
    }

    /**
     * As vantagens vão junto. Empresa com vantagem já resgatada não sai: os cupons emitidos e
     * o extrato dos alunos dependem dela.
     */
    @Transactional
    public void excluir(String cnpj) {
        Empresa empresa = encontrar(cnpj);
        if (trocas.existsByVantagemEmpresaCnpj(empresa.getCnpj())) {
            throw ApiException.empresaComResgates();
        }
        vantagens.removerTodasDa(empresa.getCnpj());
        empresas.delete(empresa);
    }

    private Empresa encontrar(String cnpj) {
        return empresas.findById(Documentos.normalizarCnpj(cnpj))
                .orElseThrow(ApiException::empresaNaoEncontrada);
    }

}
