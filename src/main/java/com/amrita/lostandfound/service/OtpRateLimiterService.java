package com.amrita.lostandfound.service;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OtpRateLimiterService {

    // This map remembers the bucket for each specific email address
    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();

    public Bucket resolveBucket(String email) {
        // If the email already has a bucket, return it. Otherwise, create a new strict one.
        return cache.computeIfAbsent(email, this::newBucket);
    }

    private Bucket newBucket(String email) {
        // THE RULES:
        // Users get a maximum of 3 OTP requests.
        // It refills at a rate of 3 tokens every 15 minutes.
        Refill refill = Refill.intervally(5, Duration.ofMinutes(15));
        Bandwidth limit = Bandwidth.classic(5, refill);

        return Bucket.builder()
                .addLimit(limit)
                .build();
    }
}