package com.egon.dev;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.egon.dto.SessaoResponse;
import com.egon.exception.ApiException;
import com.egon.repository.AdminRepository;
import com.egon.repository.AlunoRepository;
import com.egon.repository.EmpresaRepository;
import com.egon.repository.ProfessorRepository;
import com.egon.security.ContaLogada;
import com.egon.security.ContasService;
import com.egon.security.Perfil;
import com.egon.security.Sessoes;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Modo dev: lista todas as contas e entra em qualquer uma sem senha, para testar à mão (no front,
 * pelo código Konami na tela de login). Só existe com {@code egon.dev=true} (perfil dev do Spring ou
 * EGON_DEV=true); desligado, que é o padrão, estas rotas nem existem.
 */
@Slf4j
@RestController
@RequestMapping("/dev")
@ConditionalOnProperty(name = "egon.dev", havingValue = "true")
@RequiredArgsConstructor
class ModoDevController {

    record Conta(Perfil perfil, UUID id, String nome, String login, String detalhe) {
    }

    record Entrar(@NotNull Perfil perfil, @NotNull UUID id) {
    }

    private final AlunoRepository alunos;
    private final ProfessorRepository professores;
    private final EmpresaRepository empresas;
    private final AdminRepository admins;
    private final ContasService contas;
    private final Sessoes sessoes;

    @PostConstruct
    void avisar() {
        log.warn("Modo dev ligado: qualquer pessoa entra em qualquer conta sem senha por /dev. Nunca ligue em produção.");
    }

    @GetMapping("/contas")
    @Transactional(readOnly = true)
    List<Conta> contas() {
        return Stream.of(
                        alunos.findAllByOrderByNomeAsc().stream().map(a -> new Conta(Perfil.ALUNO, a.getId(), a.getNome(),
                                a.getLogin(), a.getCurso().getNome() + " · " + a.getInstituicao().getNome())),
                        professores.findAllByOrderByNomeAsc().stream().map(p -> new Conta(Perfil.PROFESSOR, p.getId(),
                                p.getNome(), p.getLogin(), p.getDepartamento() + " · " + p.getInstituicao().getNome())),
                        empresas.findAllByOrderByNomeAsc().stream().map(e -> new Conta(Perfil.EMPRESA, e.getId(),
                                e.getNome(), e.getLogin(), "Empresa parceira")),
                        admins.findAllByOrderByNomeAsc().stream().map(a -> new Conta(Perfil.ADMIN, a.getId(), a.getNome(),
                                a.getLogin(), "Admin")))
                .flatMap(s -> s)
                .sorted(Comparator.comparing(Conta::perfil))
                .toList();
    }

    @PostMapping("/entrar")
    SessaoResponse entrar(@Valid @RequestBody Entrar requisicao, HttpServletRequest http, HttpServletResponse resposta) {
        ContaLogada conta = contas.porId(requisicao.perfil(), requisicao.id())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "CONTA_NAO_ENCONTRADA", null, "Conta não encontrada."));
        return sessoes.abrir(UsernamePasswordAuthenticationToken.authenticated(conta, null, conta.getAuthorities()), http, resposta);
    }

}
