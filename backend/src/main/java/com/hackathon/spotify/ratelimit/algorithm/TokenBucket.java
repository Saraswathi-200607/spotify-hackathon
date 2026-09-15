package com.hackathon.spotify.ratelimit.algorithm;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class TokenBucket {

    private final StringRedisTemplate redisTemplate;

    public TokenBucket(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    private final DefaultRedisScript<String> script =
            new DefaultRedisScript<>(
                    """
                    local time = redis.call('TIME')

                    local now =
                        tonumber(time[1]) * 1000 +
                        math.floor(tonumber(time[2]) / 1000)

                    local capacity = tonumber(ARGV[1])
                    local refillSeconds = tonumber(ARGV[2])

                    local refillPerMs =
                        capacity / (refillSeconds * 1000)

                    local tokens =
                        tonumber(redis.call('HGET', KEYS[1], 'tokens'))

                    local last =
                        tonumber(redis.call('HGET', KEYS[1], 'last'))

                    if tokens == nil then
                        tokens = capacity
                        last = now
                    end

                    local elapsed = math.max(0, now - last)

                    tokens = math.min(
                        capacity,
                        tokens + elapsed * refillPerMs
                    )

                    local allowed = 0
                    local retry = 0

                    if tokens >= 1 then

                        tokens = tokens - 1
                        allowed = 1

                    else

                        retry = math.ceil(
                            (1 - tokens) /
                            refillPerMs /
                            1000
                        )

                        retry = math.max(1, retry)

                    end

                    redis.call(
                        'HSET',
                        KEYS[1],
                        'tokens',
                        tokens,
                        'last',
                        now
                    )

                    redis.call(
                        'EXPIRE',
                        KEYS[1],
                        refillSeconds * 2
                    )

                    local remaining =
                        math.floor(tokens)

                    return allowed ..
                           "|" ..
                           remaining ..
                           "|" ..
                           retry
                    """,
                    String.class
            );

    public long[] allowRequest(
            String key,
            int capacity,
            int refillSeconds) {

        String result = redisTemplate.execute(
                script,
                Collections.singletonList(key),
                String.valueOf(capacity),
                String.valueOf(refillSeconds)
        );

        String[] parts = result.split("\\|");

        return new long[]{
                Long.parseLong(parts[0]),
                Long.parseLong(parts[1]),
                Long.parseLong(parts[2])
        };
    }
}