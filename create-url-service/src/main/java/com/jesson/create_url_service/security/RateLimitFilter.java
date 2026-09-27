package com.jesson.create_url_service.security;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitFilter implements Filter {

    /** NOTE: one bucket per client IP, held in memory **/
    private final ConcurrentHashMap<String, Bucket> buckets = new ConcurrentHashMap<>();

    private String resolveClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            // X-Forwarded-For can be a chain: "client, proxy1, proxy2" — the first entry is the original client
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        if (!request.getRequestURI().equals("/api/v1/create-url")) {
            chain.doFilter(req, res);
            return;
        }

        String clientIp = request.getRemoteAddr();
        Bucket bucket = buckets.computeIfAbsent(clientIp, ip -> newBucket());

        if (bucket.tryConsume(1)) {
            chain.doFilter(req, res);
        } else {
            response.setStatus(429);
            response.setContentType("application/json");
            response.getWriter().write(
                    "{\"status\":429,\"error\":\"Too Many Requests\",\"message\":\"Rate limit exceeded. Please try again later.\"}"
            );
        }
    }

    private Bucket newBucket() {
        /**  10 requests per minute, per IP **/
        Bandwidth limit = Bandwidth.builder()
                .capacity(10)
                .refillIntervally(10, Duration.ofMinutes(1))
                .build();

        return Bucket.builder().addLimit(limit).build();
    }
}