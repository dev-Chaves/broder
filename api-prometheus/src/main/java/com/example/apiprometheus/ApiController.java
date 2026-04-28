package com.example.apiprometheus;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
public class ApiController {

    private final List<byte[]> memoryHog = new ArrayList<>();

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

    @GetMapping("/error-500")
    public Map<String, Object> error500() {
        throw new RuntimeException("Simulated 500 error for load testing");
    }

    @GetMapping("/error-400")
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> error400() {
        return Map.of("error", "Simulated 400 error for load testing");
    }

    @GetMapping("/cpu")
    public Map<String, Object> cpuStress(@RequestParam(defaultValue = "1000000") int iterations) {
        double result = 0;
        for (int i = 0; i < iterations; i++) {
            result += Math.sin(i) * Math.cos(i) * Math.sqrt(i + 1);
        }
        return Map.of(
                "message", "CPU stress completed",
                "iterations", iterations,
                "result", result
        );
    }

    @GetMapping("/memory")
    public Map<String, Object> memoryStress(@RequestParam(defaultValue = "50") int megabytes) {
        int bytes = megabytes * 1024 * 1024;
        memoryHog.add(new byte[bytes]);
        long totalAllocated = memoryHog.size() * (long) bytes;
        return Map.of(
                "message", "Memory allocated",
                "megabytes", megabytes,
                "totalAllocatedBytes", totalAllocated,
                "chunks", memoryHog.size()
        );
    }

    @GetMapping("/memory/clear")
    public Map<String, Object> clearMemory() {
        int chunks = memoryHog.size();
        memoryHog.clear();
        System.gc();
        return Map.of(
                "message", "Memory cleared and GC triggered",
                "chunksFreed", chunks
        );
    }
}
