package com.example.apiprometheus;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

@RestController
public class ApiController {

    @GetMapping("/hello")
    public Map<String, Object> hello() {
        return Map.of(
                "message", "Hello from Spring API!",
                "timestamp", Instant.now().toString()
        );
    }

    @GetMapping("/info")
    public Map<String, Object> info() {
        return Map.of(
                "app", "api-prometheus",
                "version", "1.0.0",
                "description", "Spring Boot API exporting metrics for Broder observation",
                "status", "UP"
        );
    }
}
