package com.example.BI.Chat;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rate-limiting léger par IP sur POST /api/chat :
 * 6 requêtes par 10 secondes, 429 au-delà.
 */
@Component
public class ChatRateLimitInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(ChatRateLimitInterceptor.class);
    private static final int MAX_REQUESTS = 6;
    private static final long WINDOW_MS = 10_000L;
    private static final int MAX_IPS = 10_000;

    private final Map<String, Deque<Long>> buckets = new ConcurrentHashMap<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) throws Exception {
        if (!"POST".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        String ip = clientIp(request);
        Deque<Long> timestamps = buckets.computeIfAbsent(ip, k -> new ArrayDeque<>());
        long now = System.currentTimeMillis();

        synchronized (timestamps) {
            while (!timestamps.isEmpty() && now - timestamps.peekFirst() > WINDOW_MS) {
                timestamps.pollFirst();
            }
            if (timestamps.size() >= MAX_REQUESTS) {
                logger.warn("[RATE-LIMIT] 429 pour l'IP {}", ip);
                response.setStatus(429);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"error\":\"Too many requests\"}");
                return false;
            }
            timestamps.addLast(now);
        }

        // nettoyage opportuniste pour éviter la croissance non bornée
        if (buckets.size() > MAX_IPS) {
            buckets.entrySet().removeIf(e -> e.getValue().isEmpty());
        }
        return true;
    }

    private static String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        String ip = request.getRemoteAddr();
        return ip == null ? "unknown" : ip;
    }
}
