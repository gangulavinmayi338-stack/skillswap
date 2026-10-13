package com.skillswap.skillswap.controller;

import com.skillswap.skillswap.entity.Availability;
import com.skillswap.skillswap.service.AvailabilityService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = {"http://localhost:5173", "https://skillswap-puce-xi.vercel.app", "${APP_FRONTEND_URL:http://localhost:5173}"})
public class AvailabilityController {

    private final AvailabilityService availabilityService;

    public AvailabilityController(
            AvailabilityService availabilityService
    ) {
        this.availabilityService = availabilityService;
    }

    @PostMapping("/api/users/{userId}/availability")
    public ResponseEntity<Availability> addAvailability(
            @PathVariable Long userId,
            @RequestBody Availability availability
    ) {
        return ResponseEntity.ok(
                availabilityService.addAvailability(
                        userId,
                        availability
                )
        );
    }

    @GetMapping("/api/users/{userId}/availability")
    public ResponseEntity<List<Availability>> getUserAvailability(
            @PathVariable Long userId
    ) {
        return ResponseEntity.ok(
                availabilityService.getUserAvailability(userId)
        );
    }

    @PutMapping("/api/availability/{id}")
    public ResponseEntity<Availability> updateAvailability(
            @PathVariable Long id,
            @RequestBody Availability availability
    ) {
        return ResponseEntity.ok(
                availabilityService.updateAvailability(
                        id,
                        availability
                )
        );
    }

    @DeleteMapping("/api/availability/{id}")
    public ResponseEntity<String> deleteAvailability(
            @PathVariable Long id
    ) {
        availabilityService.deleteAvailability(id);

        return ResponseEntity.ok(
                "Availability deleted successfully"
        );
    }
}