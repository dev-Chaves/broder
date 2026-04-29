package org.acme.domain.alarm;

import org.acme.domain.alarm.dto.AlarmTemplate;
import org.acme.domain.alarm.dto.FilterInfoDTO;

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
                    List.of(
                            new FilterInfoDTO("area", "Memory area", "heap", "Part of JVM memory: heap (objects) or nonheap (code/metaspace)"),
                            new FilterInfoDTO("id", "Memory pool", "G1 Old Gen", "Specific region inside the memory area")
                    ),
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
                    List.of(
                            new FilterInfoDTO("area", "Memory area", "heap", "Part of JVM memory: heap (objects) or nonheap (code/metaspace)"),
                            new FilterInfoDTO("id", "Memory pool", "G1 Old Gen", "Specific region inside the memory area")
                    ),
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
                    List.of(
                            new FilterInfoDTO("uri", "Endpoint path", "/api/users", "URL path of the request you want to monitor. Use /** to match all endpoints."),
                            new FilterInfoDTO("method", "HTTP method", "GET, POST", "The HTTP verb. Examples: GET, POST, PUT, DELETE."),
                            new FilterInfoDTO("status", "HTTP status code", "200, 500", "The numeric response code. Examples: 200 (OK), 404 (Not Found), 500 (Server Error)."),
                            new FilterInfoDTO("outcome", "Request result", "SERVER_ERROR", "How the request ended: SUCCESS (2xx), CLIENT_ERROR (4xx), SERVER_ERROR (5xx).")
                    ),
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
                    List.of(
                            new FilterInfoDTO("uri", "Endpoint path", "/api/users", "URL path of the request you want to monitor. Use /** to match all endpoints."),
                            new FilterInfoDTO("method", "HTTP method", "GET, POST", "The HTTP verb. Examples: GET, POST, PUT, DELETE."),
                            new FilterInfoDTO("status", "HTTP status code", "200, 500", "The numeric response code. Examples: 200 (OK), 404 (Not Found), 500 (Server Error)."),
                            new FilterInfoDTO("outcome", "Request result", "CLIENT_ERROR", "How the request ended: SUCCESS (2xx), CLIENT_ERROR (4xx), SERVER_ERROR (5xx).")
                    ),
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
                    List.of(
                            new FilterInfoDTO("uri", "Endpoint path", "/api/users", "URL path of the request you want to monitor. Use /** to match all endpoints."),
                            new FilterInfoDTO("method", "HTTP method", "GET, POST", "The HTTP verb. Examples: GET, POST, PUT, DELETE."),
                            new FilterInfoDTO("status", "HTTP status code", "200, 500", "The numeric response code. Examples: 200 (OK), 404 (Not Found), 500 (Server Error)."),
                            new FilterInfoDTO("outcome", "Request result", "SUCCESS", "How the request ended: SUCCESS (2xx), CLIENT_ERROR (4xx), SERVER_ERROR (5xx).")
                    ),
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
                    List.of(
                            new FilterInfoDTO("uri", "Endpoint path", "/api/users", "URL path of the request you want to monitor. Use /** to match all endpoints."),
                            new FilterInfoDTO("method", "HTTP method", "GET, POST", "The HTTP verb. Examples: GET, POST, PUT, DELETE."),
                            new FilterInfoDTO("status", "HTTP status code", "200, 500", "The numeric response code. Examples: 200 (OK), 404 (Not Found), 500 (Server Error)."),
                            new FilterInfoDTO("outcome", "Request result", "SUCCESS", "How the request ended: SUCCESS (2xx), CLIENT_ERROR (4xx), SERVER_ERROR (5xx).")
                    ),
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
                    List.of(
                            new FilterInfoDTO("uri", "Endpoint path", "/api/users", "URL path of the request you want to monitor. Use /** to match all endpoints."),
                            new FilterInfoDTO("method", "HTTP method", "GET, POST", "The HTTP verb. Examples: GET, POST, PUT, DELETE."),
                            new FilterInfoDTO("status", "HTTP status code", "200, 500", "The numeric response code. Examples: 200 (OK), 404 (Not Found), 500 (Server Error)."),
                            new FilterInfoDTO("outcome", "Request result", "SUCCESS", "How the request ended: SUCCESS (2xx), CLIENT_ERROR (4xx), SERVER_ERROR (5xx).")
                    ),
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
                    List.of(
                            new FilterInfoDTO("uri", "Endpoint path", "/api/users", "URL path of the request you want to monitor. Use /** to match all endpoints."),
                            new FilterInfoDTO("method", "HTTP method", "GET, POST", "The HTTP verb. Examples: GET, POST, PUT, DELETE."),
                            new FilterInfoDTO("status", "HTTP status code", "200, 500", "The numeric response code. Examples: 200 (OK), 404 (Not Found), 500 (Server Error)."),
                            new FilterInfoDTO("outcome", "Request result", "SUCCESS", "How the request ended: SUCCESS (2xx), CLIENT_ERROR (4xx), SERVER_ERROR (5xx).")
                    ),
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
                    List.of(
                            new FilterInfoDTO("uri", "Endpoint path", "/api/users", "URL path of the request you want to monitor. Use /** to match all endpoints."),
                            new FilterInfoDTO("method", "HTTP method", "GET, POST", "The HTTP verb. Examples: GET, POST, PUT, DELETE."),
                            new FilterInfoDTO("status", "HTTP status code", "200, 500", "The numeric response code. Examples: 200 (OK), 404 (Not Found), 500 (Server Error)."),
                            new FilterInfoDTO("outcome", "Request result", "SUCCESS", "How the request ended: SUCCESS (2xx), CLIENT_ERROR (4xx), SERVER_ERROR (5xx).")
                    ),
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
                    List.of(
                            new FilterInfoDTO("gc", "Garbage collector", "G1 Young Generation", "Name of the GC algorithm collecting memory"),
                            new FilterInfoDTO("action", "GC action type", "end of minor GC", "What the GC did: minor GC (young), major GC (old), etc.")
                    ),
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
                    List.of(
                            new FilterInfoDTO("pool", "Database pool", "default", "Name of the database connection pool"),
                            new FilterInfoDTO("id", "Connection ID", "hikari-pool-1", "Identifier of the connection pool instance")
                    ),
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
                    List.of(
                            new FilterInfoDTO("state", "Thread state", "RUNNABLE", "What the thread is doing: RUNNABLE, BLOCKED, WAITING, etc.")
                    ),
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
