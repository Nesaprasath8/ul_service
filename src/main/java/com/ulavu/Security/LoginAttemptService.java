package com.ulavu.Security;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.stereotype.Component;

/**
 * Simple in-memory login throttle: blocks an identifier (username, lowercased)
 * after too many failed attempts within a rolling window.
 *
 * NOTE: this is in-memory only. It resets on app restart and is NOT shared
 * across multiple service instances. If this service is ever deployed with
 * more than one instance behind a load balancer, replace this with a shared
 * store (e.g. Redis) so the attempt counters are consistent across nodes.
 */
@Component
public class LoginAttemptService {

    private static final int MAX_ATTEMPTS = 5;
    private static final long WINDOW_MS = 15 * 60 * 1000L; // 15 minutes

    private static class Attempt {
        final AtomicInteger count = new AtomicInteger(0);
        volatile long windowStart = System.currentTimeMillis();
    }

    private final ConcurrentHashMap<String, Attempt> attempts = new ConcurrentHashMap<>();

    public boolean isBlocked(String key) {
        Attempt a = attempts.get(key);
        if (a == null) {
            return false;
        }
        if (System.currentTimeMillis() - a.windowStart > WINDOW_MS) {
            attempts.remove(key);
            return false;
        }
        return a.count.get() >= MAX_ATTEMPTS;
    }

    public void recordFailure(String key) {
        Attempt a = attempts.computeIfAbsent(key, k -> new Attempt());
        if (System.currentTimeMillis() - a.windowStart > WINDOW_MS) {
            a.count.set(0);
            a.windowStart = System.currentTimeMillis();
        }
        a.count.incrementAndGet();
    }

    public void recordSuccess(String key) {
        attempts.remove(key);
    }
}
