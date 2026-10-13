package com.skillswap.skillswap.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Value("${APP_FRONTEND_URL:http://localhost:5173}")
    private String frontendUrl;

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
                .setAllowedOrigins(
                        "http://localhost:5173",
                        "https://skillswap-puce-xi.vercel.app",
                        frontendUrl
                );

        registry
                .addHandler(liveClassWebSocketHandler, "/ws/classroom")
                .setAllowedOrigins(
                        "http://localhost:5173",
                        "https://skillswap-puce-xi.vercel.app",
                        frontendUrl
                );
    }
}
