package com.skillswap.skillswap.service;

import com.skillswap.skillswap.config.LiveClassWebSocketHandler;
import com.skillswap.skillswap.entity.Session;
import com.skillswap.skillswap.entity.SessionLiveState;
import com.skillswap.skillswap.entity.SessionStatus;
import com.skillswap.skillswap.repository.SessionLiveStateRepository;
import com.skillswap.skillswap.repository.SessionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

@Service
public class SessionLiveService {

    private final SessionRepository sessions;
    private final SessionLiveStateRepository states;
    private final LiveClassWebSocketHandler socket;

    public SessionLiveService(
            SessionRepository sessions,
            SessionLiveStateRepository states,
            LiveClassWebSocketHandler socket
    ) {
        this.sessions = sessions;
        this.states = states;
        this.socket = socket;
    }

    private Session session(Long id) {
        return sessions.findById(id).orElseThrow(() -> new RuntimeException("Session not found"));
    }

    private void participant(Session session, Long userId) {
        if (!Objects.equals(session.getTeacher().getId(), userId)
                && !Objects.equals(session.getLearner().getId(), userId)) {
            throw new RuntimeException("Only session participants can access the live classroom");
        }
    }

    private void teacher(Session session, Long userId) {
        if (!Objects.equals(session.getTeacher().getId(), userId)) {
            throw new RuntimeException("Only the teacher can start or end the live class");
        }
    }

    public Map<String, Object> getState(Long sessionId, Long userId) {
        Session session = session(sessionId);
        participant(session, userId);
        SessionLiveState state = states.findBySessionId(sessionId).orElse(null);
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("active", state != null && Boolean.TRUE.equals(state.getActive()));
        out.put("startedAt", state == null ? null : state.getStartedAt());
        out.put("endedAt", state == null ? null : state.getEndedAt());
        return out;
    }

    public Map<String, Object> start(Long sessionId, Long userId) {
        Session session = session(sessionId);
        participant(session, userId);
        teacher(session, userId);
        if (session.getStatus() != SessionStatus.CONFIRMED) {
            throw new RuntimeException("Only confirmed sessions can be started");
        }

        SessionLiveState state = states.findBySessionId(sessionId).orElseGet(SessionLiveState::new);
        state.setSession(session);
        state.setActive(true);
        state.setStartedAt(LocalDateTime.now());
        state.setEndedAt(null);
        states.save(state);

        socket.broadcastEvent(sessionId, Map.of(
                "type", "class-started",
                "sessionId", sessionId,
                "startedBy", userId
        ));
        return getState(sessionId, userId);
    }

    public Map<String, Object> end(Long sessionId, Long userId) {
        Session session = session(sessionId);
        participant(session, userId);
        teacher(session, userId);

        SessionLiveState state = states.findBySessionId(sessionId)
                .orElseThrow(() -> new RuntimeException("Live class has not been started"));
        state.setActive(false);
        state.setEndedAt(LocalDateTime.now());
        states.save(state);

        if (session.getStatus() == SessionStatus.CONFIRMED) {
            session.setStatus(SessionStatus.COMPLETED);
            sessions.save(session);
        }

        socket.broadcastEvent(sessionId, Map.of(
                "type", "class-ended",
                "sessionId", sessionId,
                "endedBy", userId
        ));
        return getState(sessionId, userId);
    }
}
