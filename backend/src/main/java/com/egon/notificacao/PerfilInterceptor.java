package com.egon.notificacao;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import com.egon.security.ContaLogada;
import com.egon.security.Perfil;

/**
 * Só abre a conexão para quem está logado como aluno, professor ou empresa: a pessoa vem da sessão,
 * e ninguém escuta os avisos de outra trocando um parâmetro. O admin não recebe avisos.
 */
@Component
public class PerfilInterceptor implements HandshakeInterceptor {

    @Override
    public boolean beforeHandshake(ServerHttpRequest requisicao, ServerHttpResponse resposta,
            WebSocketHandler handler, Map<String, Object> atributos) {
        if (requisicao.getPrincipal() instanceof Authentication autenticacao
                && autenticacao.getPrincipal() instanceof ContaLogada conta
                && conta.getPerfil() != Perfil.ADMIN) {
            atributos.put(NotificacaoHandler.DESTINATARIO, NotificacaoHandler.destinatario(conta.getPerfil(), conta.getId()));
            return true;
        }
        resposta.setStatusCode(HttpStatus.FORBIDDEN);
        return false;
    }

    @Override
    public void afterHandshake(ServerHttpRequest requisicao, ServerHttpResponse resposta,
            WebSocketHandler handler, Exception erro) {
    }

}
