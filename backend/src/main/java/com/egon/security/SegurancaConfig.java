package com.egon.security;

import java.io.IOException;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
import org.springframework.security.web.context.SecurityContextHolderFilter;
import org.springframework.security.web.csrf.CsrfException;

import com.egon.exception.ErroResponse;

import jakarta.servlet.http.HttpServletResponse;
import tools.jackson.databind.json.JsonMapper;

/**
 * Login por sessão (cookie HttpOnly) e regras por perfil. Tudo nasce fechado: só o que está
 * listado aqui é público, e cada endpoint diz quem pode com {@code @PreAuthorize}.
 *
 * <p>CSRF no padrão de SPA: o token vai num cookie que o front lê e devolve no cabeçalho
 * {@code X-XSRF-TOKEN} em toda escrita, inclusive no login e nos cadastros públicos.
 */
@Configuration
@EnableMethodSecurity
class SegurancaConfig {

    @Bean
    SecurityFilterChain filtros(HttpSecurity http, ContasService contas, JsonMapper json) throws Exception {
        http.csrf(csrf -> csrf.spa())
                .authorizeHttpRequests(regras -> regras
                        // /dev/** só existe com egon.dev=true (ModoDevController); desligado, é 404.
                        .requestMatchers("/auth/**", "/ws/**", "/dev/**", "/error").permitAll()
                        .requestMatchers(HttpMethod.POST, "/alunos", "/empresas").permitAll()
                        .requestMatchers(HttpMethod.GET, "/instituicoes", "/cursos").permitAll()
                        .anyRequest().authenticated())
                .exceptionHandling(erros -> erros
                        .authenticationEntryPoint((requisicao, resposta, e) -> escrever(json, resposta,
                                HttpStatus.UNAUTHORIZED, new ErroResponse("NAO_AUTENTICADO", "Entre com seu login para continuar.")))
                        .accessDeniedHandler((requisicao, resposta, e) -> escrever(json, resposta, HttpStatus.FORBIDDEN,
                                e instanceof CsrfException
                                        ? new ErroResponse("CSRF_INVALIDO", "A página ficou desatualizada. Recarregue e tente de novo.")
                                        : new ErroResponse("ACESSO_NEGADO", "Você não tem acesso a isto."))))
                .logout(saida -> saida
                        .logoutUrl("/auth/logout")
                        .logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler(HttpStatus.NO_CONTENT)))
                .addFilterAfter(new ContaAtivaFilter(contas), SecurityContextHolderFilter.class)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable);
        return http.build();
    }

    @Bean
    AuthenticationManager autenticacao(ContasService contas, PasswordEncoder senhas) {
        DaoAuthenticationProvider provedor = new DaoAuthenticationProvider(contas);
        provedor.setPasswordEncoder(senhas);
        return new ProviderManager(provedor);
    }

    private static void escrever(JsonMapper json, HttpServletResponse resposta, HttpStatus status, ErroResponse erro)
            throws IOException {
        resposta.setStatus(status.value());
        resposta.setContentType(MediaType.APPLICATION_JSON_VALUE);
        resposta.setCharacterEncoding("UTF-8");
        json.writeValue(resposta.getOutputStream(), erro);
    }

}
