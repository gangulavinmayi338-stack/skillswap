package com.skillswap.skillswap.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final MessageWebSocketHandler messageWebSocketHandler;
    private final LiveClassWebSocketHandler liveClassWebSocketHandler;

    public WebSocketConfig(
            MessageWebSocketHandler messageWebSocketHandler,
            LiveClassWebSocketHandler liveClassWebSocketHandler
    ) {
        this.messageWebSocketHandler = messageWebSocketHandler;
        this.liveClassWebSocketHandler = liveClassWebSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry
                .addHandler(messageWebSocketHandler, "/ws/messages")
                .setAllowedOrigins("http://localhost:5173");

        registry
                .addHandler(liveClassWebSocketHandler, "/ws/classroom")
                .setAllowedOrigins("http://localhost:5173");
    }
}
