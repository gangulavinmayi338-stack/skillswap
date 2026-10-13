package com.skillswap.skillswap.controller;

import com.skillswap.skillswap.entity.Connection;
import com.skillswap.skillswap.service.ConnectionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/connections")
@CrossOrigin(origins = "${APP_FRONTEND_URL:http://localhost:5173}")
public class ConnectionController {

    private final ConnectionService connectionService;

    public ConnectionController(
            ConnectionService connectionService
    ) {
        this.connectionService = connectionService;
    }

    @PostMapping
    public ResponseEntity<Connection> sendConnectionRequest(
            @RequestBody Map<String, Long> request
    ) {

        Long senderId = request.get("senderId");
        Long receiverId = request.get("receiverId");

        return ResponseEntity.ok(
                connectionService.sendConnectionRequest(
                        senderId,
                        receiverId
                )
        );
    }

    @GetMapping("/{userId}")
    public ResponseEntity<List<Connection>> getUserConnections(
            @PathVariable Long userId
    ) {

        return ResponseEntity.ok(
                connectionService.getUserConnections(userId)
        );
    }

    @GetMapping("/{userId}/pending")
    public ResponseEntity<List<Connection>> getPendingRequests(
            @PathVariable Long userId
    ) {

        return ResponseEntity.ok(
                connectionService.getPendingRequests(userId)
        );
    }

    @PutMapping("/{id}/accept")
    public ResponseEntity<Connection> acceptConnection(
            @PathVariable Long id
    ) {

        return ResponseEntity.ok(
                connectionService.acceptConnection(id)
        );
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<Connection> rejectConnection(
            @PathVariable Long id
    ) {

        return ResponseEntity.ok(
                connectionService.rejectConnection(id)
        );
    }
}