package com.example.salarymanagement.controller;

import com.example.salarymanagement.dto.CountrySummaryDTO;
import com.example.salarymanagement.dto.DepartmentSummaryDTO;
import com.example.salarymanagement.dto.SalaryAnalyticsDTO;
import com.example.salarymanagement.dto.TopEarnerDTO;
import com.example.salarymanagement.service.AnalyticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/summary")
    public ResponseEntity<SalaryAnalyticsDTO> getSummaryAnalytics() {
        SalaryAnalyticsDTO summary = analyticsService.getSummaryAnalytics();
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/breakdown/country")
    public ResponseEntity<List<CountrySummaryDTO>> getCountryBreakdown() {
        List<CountrySummaryDTO> breakdown = analyticsService.getCountryBreakdown();
        return ResponseEntity.ok(breakdown);
    }

    @GetMapping("/breakdown/department")
    public ResponseEntity<List<DepartmentSummaryDTO>> getDepartmentBreakdown() {
        List<DepartmentSummaryDTO> breakdown = analyticsService.getDepartmentBreakdown();
        return ResponseEntity.ok(breakdown);
    }

    @GetMapping("/top-earners")
    public ResponseEntity<List<TopEarnerDTO>> getTopEarners(
            @RequestParam(required = false) String countryCode,
            @RequestParam(defaultValue = "10") int limit) {
        List<TopEarnerDTO> topEarners = analyticsService.getTopEarners(countryCode, limit);
        return ResponseEntity.ok(topEarners);
    }
}
