package com.egon.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import com.egon.notificacao.NotificacaoHandler;
import com.egon.notificacao.PerfilInterceptor;

import lombok.RequiredArgsConstructor;

/** WebSocket puro, sem STOMP: o servidor só empurra avisos, o cliente não manda nada. */
@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

    private final NotificacaoHandler notificacoes;
    private final PerfilInterceptor perfis;

    /** Configurável porque o front em dev chega pelo proxy do Vite, com a Origin dele. */
    @Value("${egon.ws.origens}")
    private String[] origens;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registro) {
        registro.addHandler(notificacoes, "/ws/notificacoes")
                .addInterceptors(perfis)
                .setAllowedOriginPatterns(origens);
    }

}
