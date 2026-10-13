package com.skillswap.skillswap.controller;

import com.skillswap.skillswap.entity.Rating;
import com.skillswap.skillswap.service.RatingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:5173")
public class RatingController {

    private final RatingService ratingService;

    public RatingController(RatingService ratingService) {
        this.ratingService = ratingService;
    }

    @PostMapping("/ratings")
    public ResponseEntity<Rating> createRating(
            @RequestBody Map<String, Object> request
    ) {

        Long sessionId =
                Long.valueOf(request.get("sessionId").toString());

        Long reviewerId =
                Long.valueOf(request.get("reviewerId").toString());

        Long reviewedUserId =
                Long.valueOf(request.get("reviewedUserId").toString());

        Integer rating =
                Integer.valueOf(request.get("rating").toString());

        String feedback =
                request.get("feedback") == null
                        ? null
                        : request.get("feedback").toString();

        return ResponseEntity.ok(
                ratingService.createRating(
                        sessionId,
                        reviewerId,
                        reviewedUserId,
                        rating,
                        feedback
                )
        );
    }

    @GetMapping("/users/{userId}/ratings")
    public ResponseEntity<List<Rating>> getUserRatings(
            @PathVariable Long userId
    ) {

        return ResponseEntity.ok(
                ratingService.getRatingsForUser(userId)
        );
    }
}