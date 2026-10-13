package com.skillswap.skillswap.controller;

import com.skillswap.skillswap.entity.Message;
import com.skillswap.skillswap.service.MessageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/messages")
@CrossOrigin(origins = "${APP_FRONTEND_URL:http://localhost:5173}")
public class MessageController {

    private final MessageService messageService;

    public MessageController(
            MessageService messageService
    ) {
        this.messageService = messageService;
    }

    @PostMapping
    public ResponseEntity<Message> sendMessage(
            @RequestBody Map<String, Object> request
    ) {

        Long senderId =
                Long.valueOf(request.get("senderId").toString());

        Long receiverId =
                Long.valueOf(request.get("receiverId").toString());

        String messageText =
                request.get("message").toString();

        return ResponseEntity.ok(
                messageService.sendMessage(
                        senderId,
                        receiverId,
                        messageText
                )
        );
    }

    @GetMapping("/{user1Id}/{user2Id}")
    public ResponseEntity<List<Message>> getConversation(
            @PathVariable Long user1Id,
            @PathVariable Long user2Id
    ) {

        return ResponseEntity.ok(
                messageService.getConversation(
                        user1Id,
                        user2Id
                )
        );
    }
}