package com.hackathon.spotify.ratelimit.algorithm;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class FixedWindowCounter {

    private final StringRedisTemplate redisTemplate;

    public FixedWindowCounter(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    private final DefaultRedisScript<String> script =
            new DefaultRedisScript<>(
                    """
                    local count = redis.call('INCR', KEYS[1])

                    if count == 1 then
                        redis.call('EXPIRE', KEYS[1], ARGV[2])
                    end

                    local limit = tonumber(ARGV[1])
                    local window = tonumber(ARGV[2])

                    local remaining = math.max(0, limit - count)
                    local allowed = 0
                    local retry = 0

                    if count <= limit then
                        allowed = 1
                    else
                        local ttl = redis.call('TTL', KEYS[1])
                        retry = math.max(1, ttl)
                    end

                    return allowed .. "|" .. remaining .. "|" .. retry
                    """,
                    String.class
            );

    public long[] allowRequest(
            String key,
            int limit,
            int windowSeconds) {

        String result = redisTemplate.execute(
                script,
                Collections.singletonList(key),
                String.valueOf(limit),
                String.valueOf(windowSeconds)
        );

        String[] parts = result.split("\\|");

        return new long[]{
                Long.parseLong(parts[0]),
                Long.parseLong(parts[1]),
                Long.parseLong(parts[2])
        };
    }
}