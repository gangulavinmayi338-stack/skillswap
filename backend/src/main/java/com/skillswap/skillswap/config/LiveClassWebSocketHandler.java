package com.skillswap.skillswap.config;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class LiveClassWebSocketHandler extends TextWebSocketHandler {

    private final Map<Long, Map<Long, WebSocketSession>> rooms = new ConcurrentHashMap<>();
    private final JsonMapper jsonMapper = JsonMapper.builder().build();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Long sessionId = queryLong(session, "sessionId");
        Long userId = queryLong(session, "userId");
        if (sessionId == null || userId == null) {
            session.close(CloseStatus.BAD_DATA.withReason("sessionId and userId are required"));
            return;
        }

        Map<Long, WebSocketSession> room = rooms.computeIfAbsent(sessionId, key -> new ConcurrentHashMap<>());
        WebSocketSession old = room.put(userId, session);
        if (old != null && old != session && old.isOpen()) {
            try { old.close(); } catch (Exception ignored) {}
        }

        for (Map.Entry<Long, WebSocketSession> entry : room.entrySet()) {
            if (entry.getValue() != null && entry.getValue().isOpen()) {
                sendEvent(session, Map.of("type", "presence", "userId", entry.getKey(), "online", true));
            }
        }
        broadcastEvent(sessionId, Map.of("type", "presence", "userId", userId, "online", true));
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        Long sessionId = queryLong(session, "sessionId");
        Long userId = queryLong(session, "userId");
        if (sessionId == null || userId == null) return;

        JsonNode json = jsonMapper.readTree(message.getPayload());
        if (json == null) return;
        String type = json.has("type") ? json.get("type").asText() : "";

        if ("presence-request".equals(type)) {
            Map<Long, WebSocketSession> room = rooms.get(sessionId);
            if (room != null) {
                for (Map.Entry<Long, WebSocketSession> entry : room.entrySet()) {
                    if (entry.getValue() != null && entry.getValue().isOpen()) {
                        sendEvent(session, Map.of("type", "presence", "userId", entry.getKey(), "online", true));
                    }
                }
            }
            return;
        }

        // Client-originated classroom events are relayed to the other participant(s).
        broadcastRaw(sessionId, message.getPayload(), userId);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        removeSession(session);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        removeSession(session);
        if (session.isOpen()) session.close(CloseStatus.SERVER_ERROR);
    }

    private void removeSession(WebSocketSession session) {
        Long sessionId = queryLong(session, "sessionId");
        Long userId = queryLong(session, "userId");
        if (sessionId == null || userId == null) return;

        Map<Long, WebSocketSession> room = rooms.get(sessionId);
        if (room == null) return;
        boolean removed = room.remove(userId, session);
        if (removed) {
            broadcastEvent(sessionId, Map.of("type", "presence", "userId", userId, "online", false));
        }
        if (room.isEmpty()) rooms.remove(sessionId);
    }

    public void broadcastEvent(Long sessionId, Map<String, Object> event) {
        try {
            String payload = jsonMapper.writeValueAsString(event);
            broadcastRaw(sessionId, payload, null);
        } catch (Exception ignored) {}
    }

    private void broadcastRaw(Long sessionId, String payload, Long excludeUserId) {
        Map<Long, WebSocketSession> room = rooms.get(sessionId);
        if (room == null) return;
        for (Map.Entry<Long, WebSocketSession> entry : room.entrySet()) {
            if (excludeUserId != null && excludeUserId.equals(entry.getKey())) continue;
            WebSocketSession target = entry.getValue();
            if (target != null && target.isOpen()) {
                try { target.sendMessage(new TextMessage(payload)); } catch (Exception ignored) {}
            }
        }
    }

    private void sendEvent(WebSocketSession session, Map<String, Object> event) {
        try {
            session.sendMessage(new TextMessage(jsonMapper.writeValueAsString(event)));
        } catch (Exception ignored) {}
    }

    private Long queryLong(WebSocketSession session, String key) {
        try {
            URI uri = session.getUri();
            if (uri == null || uri.getQuery() == null) return null;
            for (String part : uri.getQuery().split("&")) {
                String[] pair = part.split("=", 2);
                if (pair.length == 2 && key.equals(pair[0])) return Long.valueOf(pair[1]);
            }
        } catch (Exception ignored) {}
        return null;
    }
}
