package com.skillswap.skillswap.controller;

import com.skillswap.skillswap.entity.*;
import com.skillswap.skillswap.service.SessionLiveService;
import com.skillswap.skillswap.service.SessionRoomService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/session-room")
@CrossOrigin(origins = "${APP_FRONTEND_URL:http://localhost:5173}")
public class SessionRoomController {
    private final SessionRoomService service;
    private final SessionLiveService liveService;

    public SessionRoomController(SessionRoomService service, SessionLiveService liveService) {
        this.service = service;
        this.liveService = liveService;
    }

    @GetMapping("/{sessionId}")
    public Map<String, Object> room(@PathVariable Long sessionId) {
        return service.room(sessionId);
    }

    @PostMapping("/{sessionId}/topics")
    public SessionTopic addTopic(@PathVariable Long sessionId, @RequestBody Map<String, Object> b) {
        return service.addTopic(sessionId, id(b, "userId"), str(b, "title"));
    }

    @PutMapping("/topics/{id}/toggle")
    public SessionTopic toggleTopic(@PathVariable Long id, @RequestBody Map<String, Object> b) {
        return service.toggleTopic(id, id(b, "userId"));
    }

    @DeleteMapping("/topics/{id}")
    public ResponseEntity<Void> deleteTopic(@PathVariable Long id, @RequestParam Long userId) {
        service.deleteTopic(id, userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{sessionId}/notes")
    public SessionNote addNote(@PathVariable Long sessionId, @RequestBody Map<String, Object> b) {
        return service.addNote(sessionId, id(b, "userId"), str(b, "content"));
    }

    @PutMapping("/notes/{id}")
    public SessionNote updateNote(@PathVariable Long id, @RequestBody Map<String, Object> b) {
        return service.updateNote(id, id(b, "userId"), str(b, "content"));
    }

    @DeleteMapping("/notes/{id}")
    public ResponseEntity<Void> deleteNote(@PathVariable Long id, @RequestParam Long userId) {
        service.deleteNote(id, userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{sessionId}/tasks")
    public SessionTask addTask(@PathVariable Long sessionId, @RequestBody Map<String, Object> b) {
        return service.addTask(sessionId, id(b, "userId"), str(b, "title"), str(b, "description"));
    }

    @PutMapping("/tasks/{id}/toggle")
    public SessionTask toggleTask(@PathVariable Long id, @RequestBody Map<String, Object> b) {
        return service.toggleTask(id, id(b, "userId"));
    }

    @DeleteMapping("/tasks/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id, @RequestParam Long userId) {
        service.deleteTask(id, userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{sessionId}/resources")
    public SessionResource addResource(@PathVariable Long sessionId, @RequestBody Map<String, Object> b) {
        return service.addResource(sessionId, id(b, "userId"), str(b, "title"), str(b, "url"), str(b, "resourceType"));
    }

    @DeleteMapping("/resources/{id}")
    public ResponseEntity<Void> deleteResource(@PathVariable Long id, @RequestParam Long userId) {
        service.deleteResource(id, userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{sessionId}/chat")
    public SessionClassMessage sendClassMessage(@PathVariable Long sessionId, @RequestBody Map<String, Object> b) {
        return service.addClassMessage(sessionId, id(b, "userId"), str(b, "message"));
    }

    @GetMapping("/{sessionId}/live")
    public Map<String, Object> liveState(@PathVariable Long sessionId, @RequestParam Long userId) {
        return liveService.getState(sessionId, userId);
    }

    @PostMapping("/{sessionId}/live/start")
    public Map<String, Object> startClass(@PathVariable Long sessionId, @RequestBody Map<String, Object> b) {
        return liveService.start(sessionId, id(b, "userId"));
    }

    @PostMapping("/{sessionId}/live/end")
    public Map<String, Object> endClass(@PathVariable Long sessionId, @RequestBody Map<String, Object> b) {
        return liveService.end(sessionId, id(b, "userId"));
    }

    private Long id(Map<String, Object> b, String k) {
        Object v = b.get(k);
        if (v == null) throw new RuntimeException(k + " is required");
        return Long.valueOf(v.toString());
    }

    private String str(Map<String, Object> b, String k) {
        Object v = b.get(k);
        return v == null ? null : v.toString();
    }
}
