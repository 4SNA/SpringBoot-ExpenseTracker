package com.Project.ExpenseTracker.controller;

import com.Project.ExpenseTracker.services.ChartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/charts")
@RequiredArgsConstructor
@CrossOrigin("*")
@PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
public class ChartController {

    private final ChartService chartService;

    @GetMapping("/category-wise")
    public ResponseEntity<?> getCategoryWiseChart(Authentication authentication) {
        try {
            Map<String, Object> chartData = chartService.getCategoryWiseExpenses(authentication.getName());
            return ResponseEntity.ok(chartData);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error generating category chart", "message", e.getMessage()));
        }
    }

    @GetMapping("/monthly-trends")
    public ResponseEntity<?> getMonthlyTrends(
            @RequestParam(required = false, defaultValue = "12") int months,
            Authentication authentication) {
        try {
            Map<String, Object> chartData = chartService.getMonthlyTrends(authentication.getName(), months);
            return ResponseEntity.ok(chartData);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error generating monthly trends", "message", e.getMessage()));
        }
    }

    @GetMapping("/daily-expenses")
    public ResponseEntity<?> getDailyExpenses(
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            Authentication authentication) {
        try {
            if (startDate == null) startDate = LocalDate.now().minusDays(30);
            if (endDate == null) endDate = LocalDate.now();

            Map<String, Object> chartData = chartService.getDailyExpenses(
                    authentication.getName(), startDate, endDate);
            return ResponseEntity.ok(chartData);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error generating daily expenses chart", "message", e.getMessage()));
        }
    }

    @GetMapping("/expense-distribution")
    public ResponseEntity<?> getExpenseDistribution(Authentication authentication) {
        try {
            Map<String, Object> chartData = chartService.getExpenseDistribution(authentication.getName());
            return ResponseEntity.ok(chartData);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error generating expense distribution", "message", e.getMessage()));
        }
    }

    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboardData(Authentication authentication) {
        try {
            Map<String, Object> dashboardData = chartService.getDashboardData(authentication.getName());
            return ResponseEntity.ok(dashboardData);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error generating dashboard data", "message", e.getMessage()));
        }
    }
}
