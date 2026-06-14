package com.distribution.config;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.concurrent.TimeUnit;

/** Ghi thời gian xử lý mỗi request vào Micrometer Timer "http.api.timing" (tag uri, method). */
@Component
public class RequestTimingInterceptor implements HandlerInterceptor {

    private final MeterRegistry registry;

    public RequestTimingInterceptor(MeterRegistry registry) { this.registry = registry; }

    @Override
    public boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object handler) {
        req.setAttribute("startNanos", System.nanoTime());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest req, HttpServletResponse res, Object handler, Exception ex) {
        Object start = req.getAttribute("startNanos");
        if (start == null) return;
        long elapsed = System.nanoTime() - (long) start;
        String uri = req.getRequestURI();
        if (!uri.startsWith("/api")) return;
        Timer.builder("http.api.timing")
            .tag("uri", uri)
            .tag("method", req.getMethod())
            .register(registry)
            .record(elapsed, TimeUnit.NANOSECONDS);
    }
}
