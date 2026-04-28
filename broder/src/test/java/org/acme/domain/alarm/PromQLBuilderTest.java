package org.acme.domain.alarm;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class PromQLBuilderTest {

    @Inject
    PromQLBuilder builder;

    @Test
    void shouldBuildSimpleQueryWithFilters() {
        String query = builder.build("cpu_usage_high", Map.of("area", "heap"));

        assertEquals("process_cpu_usage{area=\"heap\"}", query);
    }

    @Test
    void shouldBuildRateQueryWithFilters() {
        // http_p50_latency template does NOT have uri filter, so it will be added
        String query = builder.build("http_p50_latency", Map.of("uri", "/api"));

        assertEquals(
                "histogram_quantile(0.50, rate(http_server_requests_seconds_bucket{uri=\"/api\"}[1m]))",
                query);
    }

    @Test
    void shouldBuildHistogramQuantileQueryWithFilters() {
        String query = builder.build("http_p90_latency", Map.of("uri", "/api"));

        assertEquals(
                "histogram_quantile(0.90, rate(http_server_requests_seconds_bucket{uri=\"/api\"}[1m]))",
                query);
    }

    @Test
    void shouldAppendFiltersToExistingBraces() {
        // memory_heap_high already has {area="heap"} in the template
        // adding id="1" should append to existing braces in the first occurrence
        // (replace only affects the first match of the extracted metric name)
        String query = builder.build("memory_heap_high", Map.of("id", "1"));

        assertEquals(
                "(jvm_memory_used_bytes{area=\"heap\",id=\"1\"} / jvm_memory_max_bytes{area=\"heap\"})",
                query);
    }

    @Test
    void shouldRejectNullFilters() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> builder.build("cpu_usage_high", null));
        assertEquals("filters cannot be empty", ex.getMessage());
    }

    @Test
    void shouldRejectEmptyFilters() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> builder.build("cpu_usage_high", Map.of()));
        assertEquals("filters cannot be empty", ex.getMessage());
    }

    @Test
    void shouldRejectBlankFilterValues() {
        Map<String, String> blankFilter = Map.of("area", "   ");
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> builder.build("cpu_usage_high", blankFilter));
        assertEquals("filters cannot be empty", ex.getMessage());
    }

    @Test
    void shouldRejectInvalidTemplateId() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> builder.build("nonexistent", Map.of("area", "heap")));
        assertEquals("Template not found: nonexistent", ex.getMessage());
    }

    @Test
    void shouldEscapeQuotesInFilterValues() {
        String query = builder.build("cpu_usage_high", Map.of("label", "value\"with\"quotes"));

        assertEquals("process_cpu_usage{label=\"value\\\"with\\\"quotes\"}", query);
    }

    @Test
    void shouldBuildQueryWithMultipleFilters() {
        String query = builder.build("http_error_rate",
                Map.of("outcome", "SERVER_ERROR", "uri", "/api"));

        assertTrue(query.contains("outcome=\"SERVER_ERROR\""));
        assertTrue(query.contains("uri=\"/api\""));
    }
}
