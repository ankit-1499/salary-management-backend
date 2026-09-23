package com.example.salarymanagement.controller;

import com.example.salarymanagement.service.SeederService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/seed")
public class SeederController {

    private final SeederService seederService;

    public SeederController(SeederService seederService) {
        this.seederService = seederService;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> seedData() {
        long totalEmployees = seederService.seedData();
        return ResponseEntity.ok(Map.of(
                "status", "SUCCESS",
                "message", "Database successfully seeded",
                "totalEmployees", totalEmployees
        ));
    }
}
