package com.skillswap.skillswap.controller;

import com.skillswap.skillswap.service.MatchingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/matches")
@CrossOrigin(origins = "${APP_FRONTEND_URL:http://localhost:5173}")
public class MatchController {

    private final MatchingService matchingService;

    public MatchController(MatchingService matchingService) {
        this.matchingService = matchingService;
    }

    @GetMapping("/{userId}")
    public ResponseEntity<List<Map<String, Object>>> getMatches(
            @PathVariable Long userId
    ) {
        return ResponseEntity.ok(
                matchingService.getMatches(userId)
        );
    }
}