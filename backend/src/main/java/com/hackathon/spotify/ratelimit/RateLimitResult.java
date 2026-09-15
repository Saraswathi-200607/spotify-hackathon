package com.hackathon.spotify.ratelimit;

public class RateLimitResult {

    private boolean allowed;
    private int limit;
    private long remaining;
    private long retryAfter;

    public RateLimitResult(
            boolean allowed,
            int limit,
            long remaining,
            long retryAfter) {

        this.allowed = allowed;
        this.limit = limit;
        this.remaining = remaining;
        this.retryAfter = retryAfter;
    }

    public boolean isAllowed() {
        return allowed;
    }

    public int getLimit() {
        return limit;
    }

    public long getRemaining() {
        return remaining;
    }

    public long getRetryAfter() {
        return retryAfter;
    }
}