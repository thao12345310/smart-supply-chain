package com.distribution.controller;

import com.distribution.dto.ApiResponse;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/metrics")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
@PreAuthorize("hasRole('ADMIN')")
public class MetricsController {

    private final MeterRegistry registry;

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> summary() {
        List<Map<String, Object>> rows = new ArrayList<>();
        for (Timer timer : registry.find("http.api.timing").timers()) {
            rows.add(Map.of(
                "uri", timer.getId().getTag("uri"),
                "method", timer.getId().getTag("method"),
                "count", timer.count(),
                "avgMs", round(timer.mean(TimeUnit.MILLISECONDS)),
                "maxMs", round(timer.max(TimeUnit.MILLISECONDS))
            ));
        }
        rows.sort((a, b) -> Long.compare((long) b.get("count"), (long) a.get("count")));
        return ResponseEntity.ok(ApiResponse.success(rows, "Metrics summary"));
    }

    private double round(double v) { return Math.round(v * 100.0) / 100.0; }
}
