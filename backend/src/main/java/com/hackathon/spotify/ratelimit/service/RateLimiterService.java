package com.hackathon.spotify.ratelimit.service;

import com.hackathon.spotify.model.Plan;
import com.hackathon.spotify.ratelimit.RateLimitResult;
import com.hackathon.spotify.ratelimit.algorithm.FixedWindowCounter;
import com.hackathon.spotify.ratelimit.algorithm.TokenBucket;
import com.hackathon.spotify.ratelimit.algorithm.SlidingWindowCounter;

import org.springframework.stereotype.Service;

@Service
public class RateLimiterService {

    private final FixedWindowCounter fixedWindowCounter;
    private final TokenBucket tokenBucket;
    private final SlidingWindowCounter slidingWindowCounter;

    public RateLimiterService(
            FixedWindowCounter fixedWindowCounter,
            TokenBucket tokenBucket,
            SlidingWindowCounter slidingWindowCounter) {

        this.fixedWindowCounter = fixedWindowCounter;
        this.tokenBucket = tokenBucket;
        this.slidingWindowCounter = slidingWindowCounter;
    }

    public RateLimitResult checkLimit(
            String userId,
            String endpoint,
            Plan plan) {

        int limit = getLimit(endpoint, plan);

        String key =
                "rate:" + plan + ":" + userId + ":" + endpoint;

        long[] result;

        if (endpoint.equals("LOGIN")) {

            result = fixedWindowCounter.allowRequest(
                    key,
                    limit,
                    60
            );

        } else if (
                endpoint.equals("SEARCH")
                || endpoint.equals("PLAY")) {

            result = tokenBucket.allowRequest(
                    key,
                    limit,
                    60
            );

        } else {

            result = slidingWindowCounter.allowRequest(
                    key,
                    limit,
                    60
            );
        }

        boolean allowed = result[0] == 1;

        long remaining = result[1];

        long retryAfter = result[2];

        return new RateLimitResult(
                allowed,
                limit,
                remaining,
                retryAfter
        );
    }

    private int getLimit(
            String endpoint,
            Plan plan) {

        if (plan == Plan.FREE) {

            if (endpoint.equals("LOGIN")) return 5;
            if (endpoint.equals("SEARCH")) return 10;
            if (endpoint.equals("PLAY")) return 20;
            if (endpoint.equals("PLAYLIST")) return 5;

            return 10;
        }

        if (plan == Plan.PRO) {

            if (endpoint.equals("LOGIN")) return 10;
            if (endpoint.equals("SEARCH")) return 50;
            if (endpoint.equals("PLAY")) return 100;
            if (endpoint.equals("PLAYLIST")) return 20;

            return 50;
        }

        if (endpoint.equals("LOGIN")) return 20;
        if (endpoint.equals("SEARCH")) return 100;
        if (endpoint.equals("PLAY")) return 200;
        if (endpoint.equals("PLAYLIST")) return 50;

        return 100;
    }
}