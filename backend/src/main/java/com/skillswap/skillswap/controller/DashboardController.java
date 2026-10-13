package com.skillswap.skillswap.controller;

import com.skillswap.skillswap.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = {"http://localhost:5173", "https://skillswap-puce-xi.vercel.app", "${APP_FRONTEND_URL:http://localhost:5173}"})
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(
            DashboardService dashboardService
    ) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/{userId}")
    public ResponseEntity<Map<String, Object>> getDashboard(
            @PathVariable Long userId
    ) {

        return ResponseEntity.ok(
                dashboardService.getDashboard(userId)
        );
    }
}