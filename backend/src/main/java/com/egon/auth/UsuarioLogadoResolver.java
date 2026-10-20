package com.egon.auth;

import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import com.egon.exception.ApiException;

import lombok.RequiredArgsConstructor;

/** Preenche parâmetros {@link UsuarioLogado} a partir do cabeçalho {@code Authorization: Bearer <token>}. */
@Component
@RequiredArgsConstructor
public class UsuarioLogadoResolver implements HandlerMethodArgumentResolver {

    private static final String PREFIXO = "Bearer ";

    private final TokenService tokens;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterType() == UsuarioLogado.class;
    }

    @Override
    public UsuarioLogado resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
            NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        String cabecalho = webRequest.getHeader(HttpHeaders.AUTHORIZATION);
        if (cabecalho == null || !cabecalho.startsWith(PREFIXO)) {
            throw ApiException.naoAutenticado();
        }
        return tokens.ler(cabecalho.substring(PREFIXO.length()).strip())
                .orElseThrow(ApiException::naoAutenticado);
    }

}
