package com.skillswap.skillswap.controller;

import com.skillswap.skillswap.entity.Session;
import com.skillswap.skillswap.service.SessionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/sessions")
@CrossOrigin(origins = "http://localhost:5173")
public class SessionController {

    private final SessionService sessionService;

    public SessionController(
            SessionService sessionService
    ) {
        this.sessionService = sessionService;
    }

    @PostMapping
    public ResponseEntity<Session> createSession(
            @RequestBody Map<String, Object> request
    ) {

        Long teacherId =
                Long.valueOf(request.get("teacherId").toString());

        Long learnerId =
                Long.valueOf(request.get("learnerId").toString());

        Long skillId =
                Long.valueOf(request.get("skillId").toString());

        Session sessionRequest = new Session();

        sessionRequest.setSessionDate(
                java.time.LocalDate.parse(
                        request.get("sessionDate").toString()
                )
        );

        sessionRequest.setStartTime(
                java.time.LocalTime.parse(
                        request.get("startTime").toString()
                )
        );

        sessionRequest.setDuration(
                Integer.valueOf(
                        request.get("duration").toString()
                )
        );

        sessionRequest.setMode(
                request.get("mode").toString()
        );

        return ResponseEntity.ok(
                sessionService.createSession(
                        teacherId,
                        learnerId,
                        skillId,
                        sessionRequest
                )
        );
    }

    @GetMapping("/{userId}")
    public ResponseEntity<List<Session>> getUserSessions(
            @PathVariable Long userId
    ) {

        return ResponseEntity.ok(
                sessionService.getUserSessions(userId)
        );
    }

    @PutMapping("/{id}/confirm")
    public ResponseEntity<Session> confirmSession(
            @PathVariable Long id
    ) {

        return ResponseEntity.ok(
                sessionService.confirmSession(id)
        );
    }

    @PutMapping("/{id}/complete")
    public ResponseEntity<Session> completeSession(
            @PathVariable Long id
    ) {

        return ResponseEntity.ok(
                sessionService.completeSession(id)
        );
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<Session> cancelSession(
            @PathVariable Long id
    ) {

        return ResponseEntity.ok(
                sessionService.cancelSession(id)
        );
    }
}