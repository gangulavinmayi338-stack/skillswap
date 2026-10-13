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
public class MessageWebSocketHandler extends TextWebSocketHandler {

    private final Map<Long, WebSocketSession> users =
            new ConcurrentHashMap<>();

    private final JsonMapper jsonMapper =
            JsonMapper.builder().build();

    @Override
    public void afterConnectionEstablished(
            WebSocketSession session) throws Exception {

        Long userId = getUserId(session);

        if (userId == null) {
            session.close(
                    CloseStatus.BAD_DATA
                            .withReason("userId required")
            );
            return;
        }

        WebSocketSession oldSession =
                users.put(userId, session);

        if (
                oldSession != null &&
                oldSession != session &&
                oldSession.isOpen()
        ) {
            try {
                oldSession.close();
            } catch (Exception ignored) {
            }
        }

        System.out.println(
                "[MESSAGE] CONNECTED user=" + userId
        );

        /*
         * Tell all connected users
         * that this user is online
         */
        broadcastPresence(
                userId,
                true
        );
    }

    @Override
    protected void handleTextMessage(
            WebSocketSession session,
            TextMessage message) throws Exception {

        Long senderId =
                getUserId(session);

        if (senderId == null) {
            return;
        }

        JsonNode json =
                jsonMapper.readTree(
                        message.getPayload()
                );

        if (json == null) {
            return;
        }

        String type =
                json.has("type")
                        ? json.get("type").asText()
                        : "";

        /*
         * PRESENCE REQUEST
         *
         * Frontend can ask:
         * who is currently online?
         */
        if (
                "presence-request".equals(type)
        ) {
            for (
                    Map.Entry<Long, WebSocketSession> entry :
                    users.entrySet()
            ) {
                if (
                        entry.getValue() != null &&
                        entry.getValue().isOpen()
                ) {
                    sendPresence(
                            session,
                            entry.getKey(),
                            true
                    );
                }
            }

            return;
        }

        if (!json.has("to")) {
            return;
        }

        Long receiverId =
                json.get("to").asLong();

        WebSocketSession receiver =
                users.get(receiverId);

        if (
                receiver != null &&
                receiver.isOpen()
        ) {
            receiver.sendMessage(
                    new TextMessage(
                            message.getPayload()
                    )
            );
        }
    }

    @Override
    public void afterConnectionClosed(
            WebSocketSession session,
            CloseStatus status) {

        Long userId =
                getUserId(session);

        if (userId == null) {
            return;
        }

        boolean removed =
                users.remove(
                        userId,
                        session
                );

        if (removed) {
            System.out.println(
                    "[MESSAGE] DISCONNECTED user=" + userId
            );

            broadcastPresence(
                    userId,
                    false
            );
        }
    }

    @Override
    public void handleTransportError(
            WebSocketSession session,
            Throwable exception) throws Exception {

        Long userId =
                getUserId(session);

        if (userId != null) {

            boolean removed =
                    users.remove(
                            userId,
                            session
                    );

            if (removed) {
                broadcastPresence(
                        userId,
                        false
                );
            }
        }

        if (session.isOpen()) {
            session.close(
                    CloseStatus.SERVER_ERROR
            );
        }
    }

    private void broadcastPresence(
            Long userId,
            boolean online) {

        String payload =
                """
                {
                  "type": "presence",
                  "userId": %d,
                  "online": %s
                }
                """.formatted(
                        userId,
                        online
                );

        for (
                WebSocketSession userSession :
                users.values()
        ) {
            if (
                    userSession != null &&
                    userSession.isOpen()
            ) {
                try {
                    userSession.sendMessage(
                            new TextMessage(
                                    payload
                            )
                    );
                } catch (Exception ignored) {
                }
            }
        }
    }

    private void sendPresence(
            WebSocketSession session,
            Long userId,
            boolean online) {

        try {
            String payload =
                    """
                    {
                      "type": "presence",
                      "userId": %d,
                      "online": %s
                    }
                    """.formatted(
                            userId,
                            online
                    );

            session.sendMessage(
                    new TextMessage(
                            payload
                    )
            );

        } catch (Exception ignored) {
        }
    }

    private Long getUserId(
            WebSocketSession session) {

        try {
            URI uri =
                    session.getUri();

            if (uri == null) {
                return null;
            }

            String query =
                    uri.getQuery();

            if (
                    query == null ||
                    query.isBlank()
            ) {
                return null;
            }

            for (
                    String parameter :
                    query.split("&")
            ) {
                String[] parts =
                        parameter.split(
                                "=",
                                2
                        );

                if (
                        parts.length == 2 &&
                        "userId".equals(
                                parts[0]
                        )
                ) {
                    return Long.parseLong(
                            parts[1]
                    );
                }
            }

        } catch (Exception ignored) {
        }

        return null;
    }
}