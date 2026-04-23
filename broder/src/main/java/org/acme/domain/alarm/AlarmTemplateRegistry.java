package org.acme.domain.alarm;

import org.acme.domain.alarm.dto.AlarmTemplate;

import java.util.List;

public class AlarmTemplateRegistry {

    private static final List<AlarmTemplate> TEMPLATES = List.of(
            new AlarmTemplate(
                    "cpu_usage_high",
                    "CPU Usage High",
                    "Alert when CPU usage exceeds threshold",
                    "JVM",
                    "HIGH",
                    ">",
                    "0.8",
                    List.of("area", "id"),
                    "ratio (0-1)",
                    "process_cpu_usage"
            ),
            new AlarmTemplate(
                    "memory_heap_high",
                    "Memory Heap High",
                    "Alert when heap memory usage exceeds threshold",
                    "JVM",
                    "HIGH",
                    ">",
                    "0.85",
                    List.of("area", "id"),
                    "ratio (0-1)",
                    "(jvm_memory_used_bytes{area=\"heap\"} / jvm_memory_max_bytes{area=\"heap\"})"
            ),
            new AlarmTemplate(
                    "http_error_rate",
                    "HTTP Error Rate (5xx)",
                    "Alert when 5xx error rate exceeds threshold per minute",
                    "HTTP",
                    "CRITICAL",
                    ">",
                    "0.05",
                    List.of("uri", "method", "status", "outcome"),
                    "errors/sec",
                    "rate(http_server_requests_seconds_count{outcome=\"SERVER_ERROR\"}[1m])"
            ),
            new AlarmTemplate(
                    "http_client_error_rate",
                    "HTTP Client Error Rate (4xx)",
                    "Alert when 4xx error rate exceeds threshold per minute",
                    "HTTP",
                    "MEDIUM",
                    ">",
                    "0.1",
                    List.of("uri", "method", "status", "outcome"),
                    "errors/sec",
                    "rate(http_server_requests_seconds_count{outcome=\"CLIENT_ERROR\"}[1m])"
            ),
            new AlarmTemplate(
                    "http_p50_latency",
                    "P50 Latency",
                    "Alert when 50th percentile latency exceeds threshold",
                    "HTTP",
                    "LOW",
                    ">",
                    "0.5",
                    List.of("uri", "method", "status", "outcome"),
                    "seconds",
                    "histogram_quantile(0.50, rate(http_server_requests_seconds_bucket[1m]))"
            ),
            new AlarmTemplate(
                    "http_p90_latency",
                    "P90 Latency",
                    "Alert when 90th percentile latency exceeds threshold",
                    "HTTP",
                    "MEDIUM",
                    ">",
                    "1.0",
                    List.of("uri", "method", "status", "outcome"),
                    "seconds",
                    "histogram_quantile(0.90, rate(http_server_requests_seconds_bucket[1m]))"
            ),
            new AlarmTemplate(
                    "http_p99_latency",
                    "P99 Latency",
                    "Alert when 99th percentile latency exceeds threshold",
                    "HTTP",
                    "HIGH",
                    ">",
                    "2.0",
                    List.of("uri", "method", "status", "outcome"),
                    "seconds",
                    "histogram_quantile(0.99, rate(http_server_requests_seconds_bucket[1m]))"
            ),
            new AlarmTemplate(
                    "http_avg_response_time",
                    "Avg Response Time",
                    "Alert when average response time exceeds threshold",
                    "HTTP",
                    "MEDIUM",
                    ">",
                    "1.0",
                    List.of("uri", "method", "status", "outcome"),
                    "seconds",
                    "rate(http_server_requests_seconds_sum[1m]) / rate(http_server_requests_seconds_count[1m])"
            ),
            new AlarmTemplate(
                    "http_rps",
                    "Requests Per Second",
                    "Alert when requests per second exceeds threshold",
                    "HTTP",
                    "LOW",
                    ">",
                    "100",
                    List.of("uri", "method", "status", "outcome"),
                    "requests/sec",
                    "rate(http_server_requests_seconds_count[1m])"
            ),
            new AlarmTemplate(
                    "gc_pause_p95",
                    "GC Pause P95",
                    "Alert when 95th percentile GC pause exceeds threshold",
                    "JVM",
                    "HIGH",
                    ">",
                    "0.5",
                    List.of("gc", "action"),
                    "seconds",
                    "histogram_quantile(0.95, rate(jvm_gc_pause_seconds_bucket[1m]))"
            ),
            new AlarmTemplate(
                    "db_connections_high",
                    "DB Connections High",
                    "Alert when active DB connections exceed threshold",
                    "Database",
                    "HIGH",
                    ">",
                    "15",
                    List.of("pool", "id"),
                    "connections",
                    "jdbc_connections_active"
            ),
            new AlarmTemplate(
                    "thread_count_high",
                    "Thread Count High",
                    "Alert when live thread count exceeds threshold",
                    "JVM",
                    "MEDIUM",
                    ">",
                    "500",
                    List.of("state"),
                    "threads",
                    "jvm_threads_live"
            )
    );

    public static List<AlarmTemplate> all() {
        return TEMPLATES;
    }

    public static AlarmTemplate findById(String id) {
        return TEMPLATES.stream()
                .filter(t -> t.id().equals(id))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Template not found: " + id));
    }

    public static List<String> categories() {
        return TEMPLATES.stream()
                .map(AlarmTemplate::category)
                .distinct()
                .toList();
    }
}
