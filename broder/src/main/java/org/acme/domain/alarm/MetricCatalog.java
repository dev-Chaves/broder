package org.acme.domain.alarm;

import jakarta.enterprise.context.ApplicationScoped;
import org.acme.domain.alarm.dto.MetricInfoDTO;

import java.util.List;

@ApplicationScoped
public class MetricCatalog {

    public List<MetricInfoDTO> listMetrics() {
        return List.of(
                new MetricInfoDTO("process_cpu_usage", "gauge", "CPU usage ratio", List.of(), "ratio (0-1)"),
                new MetricInfoDTO("jvm_memory_used_bytes", "gauge", "JVM memory used", List.of("area", "id"), "bytes"),
                new MetricInfoDTO("jvm_memory_max_bytes", "gauge", "JVM memory max", List.of("area", "id"), "bytes"),
                new MetricInfoDTO("jvm_threads_live", "gauge", "Live threads", List.of("state"), "threads"),
                new MetricInfoDTO("jvm_gc_pause_seconds", "histogram", "GC pause duration", List.of("gc", "action"), "seconds"),
                new MetricInfoDTO("http_server_requests_seconds", "histogram", "HTTP request duration", List.of("uri", "method", "status", "outcome"), "seconds"),
                new MetricInfoDTO("http_server_requests_seconds_count", "counter", "HTTP request count", List.of("uri", "method", "status", "outcome"), "count"),
                new MetricInfoDTO("jdbc_connections_active", "gauge", "Active DB connections", List.of("pool"), "connections"),
                new MetricInfoDTO("logback_events_total", "counter", "Log events", List.of("level"), "events")
        );
    }
}
