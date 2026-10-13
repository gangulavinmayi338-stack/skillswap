package com.skillswap.skillswap.service;

import com.skillswap.skillswap.config.LiveClassWebSocketHandler;
import com.skillswap.skillswap.entity.*;
import com.skillswap.skillswap.repository.*;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class SessionRoomService {
    private final SessionRepository sessions;
    private final UserRepository users;
    private final SessionTopicRepository topics;
    private final SessionNoteRepository notes;
    private final SessionTaskRepository tasks;
    private final SessionResourceRepository resources;
    private final SessionClassMessageRepository classMessages;
    private final SessionLiveStateRepository liveStates;
    private final LiveClassWebSocketHandler socket;

    public SessionRoomService(
            SessionRepository sessions,
            UserRepository users,
            SessionTopicRepository topics,
            SessionNoteRepository notes,
            SessionTaskRepository tasks,
            SessionResourceRepository resources,
            SessionClassMessageRepository classMessages,
            SessionLiveStateRepository liveStates,
            LiveClassWebSocketHandler socket
    ) {
        this.sessions = sessions;
        this.users = users;
        this.topics = topics;
        this.notes = notes;
        this.tasks = tasks;
        this.resources = resources;
        this.classMessages = classMessages;
        this.liveStates = liveStates;
        this.socket = socket;
    }

    private Session session(Long id) {
        return sessions.findById(id).orElseThrow(() -> new RuntimeException("Session not found"));
    }

    private User user(Long id) {
        return users.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
    }

    private void participant(Session s, Long userId) {
        if (!Objects.equals(s.getTeacher().getId(), userId)
                && !Objects.equals(s.getLearner().getId(), userId)) {
            throw new RuntimeException("Only session participants can update this room");
        }
    }

    private void refresh(Long sessionId, String section) {
        socket.broadcastEvent(sessionId, Map.of(
                "type", "room-refresh",
                "section", section
        ));
    }

    public Map<String, Object> room(Long sessionId) {
        Session s = session(sessionId);
        List<SessionTopic> topicList = topics.findBySessionIdOrderByIdAsc(sessionId);
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("session", s);
        m.put("topics", topicList);
        m.put("notes", notes.findBySessionIdOrderByUpdatedAtDesc(sessionId));
        m.put("tasks", tasks.findBySessionIdOrderByIdAsc(sessionId));
        m.put("resources", resources.findBySessionIdOrderByCreatedAtDesc(sessionId));
        m.put("classMessages", classMessages.findBySessionIdOrderBySentAtAsc(sessionId));
        SessionLiveState live = liveStates.findBySessionId(sessionId).orElse(null);
        Map<String, Object> liveMap = new LinkedHashMap<>();
        liveMap.put("active", live != null && Boolean.TRUE.equals(live.getActive()));
        liveMap.put("startedAt", live == null ? null : live.getStartedAt());
        liveMap.put("endedAt", live == null ? null : live.getEndedAt());
        m.put("live", liveMap);
        int total = topicList.size();
        long done = topicList.stream().filter(t -> Boolean.TRUE.equals(t.getCompleted())).count();
        m.put("progress", total == 0 ? 0 : (int) Math.round(done * 100.0 / total));
        return m;
    }

    public SessionTopic addTopic(Long sessionId, Long userId, String title) {
        Session s = session(sessionId);
        participant(s, userId);
        if (title == null || title.isBlank()) throw new RuntimeException("Topic title is required");
        SessionTopic saved = topics.save(new SessionTopic(s, title.trim()));
        refresh(sessionId, "topics");
        return saved;
    }

    public SessionTopic toggleTopic(Long id, Long userId) {
        SessionTopic t = topics.findById(id).orElseThrow(() -> new RuntimeException("Topic not found"));
        participant(t.getSession(), userId);
        t.setCompleted(!Boolean.TRUE.equals(t.getCompleted()));
        SessionTopic saved = topics.save(t);
        refresh(t.getSession().getId(), "topics");
        return saved;
    }

    public void deleteTopic(Long id, Long userId) {
        SessionTopic t = topics.findById(id).orElseThrow(() -> new RuntimeException("Topic not found"));
        participant(t.getSession(), userId);
        Long sessionId = t.getSession().getId();
        topics.delete(t);
        refresh(sessionId, "topics");
    }

    public SessionNote addNote(Long sessionId, Long userId, String content) {
        Session s = session(sessionId);
        participant(s, userId);
        if (content == null || content.isBlank()) throw new RuntimeException("Note cannot be empty");
        SessionNote saved = notes.save(new SessionNote(s, content.trim(), user(userId)));
        refresh(sessionId, "notes");
        return saved;
    }

    public SessionNote updateNote(Long id, Long userId, String content) {
        SessionNote n = notes.findById(id).orElseThrow(() -> new RuntimeException("Note not found"));
        participant(n.getSession(), userId);
        n.setContent(content == null ? "" : content.trim());
        SessionNote saved = notes.save(n);
        refresh(n.getSession().getId(), "notes");
        return saved;
    }

    public void deleteNote(Long id, Long userId) {
        SessionNote n = notes.findById(id).orElseThrow(() -> new RuntimeException("Note not found"));
        participant(n.getSession(), userId);
        Long sessionId = n.getSession().getId();
        notes.delete(n);
        refresh(sessionId, "notes");
    }

    public SessionTask addTask(Long sessionId, Long userId, String title, String description) {
        Session s = session(sessionId);
        participant(s, userId);
        if (title == null || title.isBlank()) throw new RuntimeException("Task title is required");
        SessionTask saved = tasks.save(new SessionTask(s, title.trim(), description, user(userId)));
        refresh(sessionId, "tasks");
        return saved;
    }

    public SessionTask toggleTask(Long id, Long userId) {
        SessionTask t = tasks.findById(id).orElseThrow(() -> new RuntimeException("Task not found"));
        participant(t.getSession(), userId);
        t.setCompleted(!Boolean.TRUE.equals(t.getCompleted()));
        SessionTask saved = tasks.save(t);
        refresh(t.getSession().getId(), "tasks");
        return saved;
    }

    public void deleteTask(Long id, Long userId) {
        SessionTask t = tasks.findById(id).orElseThrow(() -> new RuntimeException("Task not found"));
        participant(t.getSession(), userId);
        Long sessionId = t.getSession().getId();
        tasks.delete(t);
        refresh(sessionId, "tasks");
    }

    public SessionResource addResource(Long sessionId, Long userId, String title, String url, String type) {
        Session s = session(sessionId);
        participant(s, userId);
        if (title == null || title.isBlank() || url == null || url.isBlank()) {
            throw new RuntimeException("Resource title and URL are required");
        }
        SessionResource saved = resources.save(new SessionResource(
                s,
                title.trim(),
                url.trim(),
                type == null ? "LINK" : type,
                user(userId)
        ));
        refresh(sessionId, "resources");
        return saved;
    }

    public void deleteResource(Long id, Long userId) {
        SessionResource r = resources.findById(id).orElseThrow(() -> new RuntimeException("Resource not found"));
        participant(r.getSession(), userId);
        Long sessionId = r.getSession().getId();
        resources.delete(r);
        refresh(sessionId, "resources");
    }

    public SessionClassMessage addClassMessage(Long sessionId, Long userId, String message) {
        Session s = session(sessionId);
        participant(s, userId);
        if (message == null || message.isBlank()) throw new RuntimeException("Message cannot be empty");
        SessionClassMessage item = new SessionClassMessage();
        item.setSession(s);
        item.setSender(user(userId));
        item.setMessage(message.trim());
        SessionClassMessage saved = classMessages.save(item);
        socket.broadcastEvent(sessionId, Map.of(
                "type", "class-chat-refresh",
                "messageId", saved.getId(),
                "senderId", userId
        ));
        return saved;
    }
}
