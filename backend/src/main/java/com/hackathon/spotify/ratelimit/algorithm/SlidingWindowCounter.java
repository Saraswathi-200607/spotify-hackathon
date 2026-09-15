package com.hackathon.spotify.ratelimit.algorithm;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
public class SlidingWindowCounter {

    private final StringRedisTemplate redisTemplate;

    public SlidingWindowCounter(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    private final DefaultRedisScript<String> script =
            new DefaultRedisScript<>(
                    """
                    local time = redis.call('TIME')

                    local now =
                        tonumber(time[1]) * 1000 +
                        math.floor(tonumber(time[2]) / 1000)

                    local window =
                        tonumber(ARGV[2]) * 1000

                    local limit =
                        tonumber(ARGV[1])

                    local currentWindow =
                        math.floor(now / window)

                    local elapsed =
                        now % window

                    local previousWindow =
                        currentWindow - 1

                    local currentKey =
                        KEYS[1] .. ":" .. currentWindow

                    local previousKey =
                        KEYS[1] .. ":" .. previousWindow

                    local current =
                        tonumber(
                            redis.call('GET', currentKey)
                        ) or 0

                    local previous =
                        tonumber(
                            redis.call('GET', previousKey)
                        ) or 0

                    local previousWeight =
                        (window - elapsed) / window

                    local estimated =
                        previous * previousWeight +
                        current

                    local allowed = 0
                    local remaining = 0
                    local retry = 0

                    if estimated < limit then

                        current =
                            redis.call(
                                'INCR',
                                currentKey
                            )

                        redis.call(
                            'EXPIRE',
                            currentKey,
                            tonumber(ARGV[2]) * 2
                        )

                        estimated =
                            estimated + 1

                        allowed = 1

                        remaining =
                            math.floor(
                                math.max(
                                    0,
                                    limit - estimated
                                )
                            )

                    else

                        remaining = 0

                        retry =
                            math.ceil(
                                (window - elapsed) / 1000
                            )

                        retry =
                            math.max(1, retry)

                    end

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
            int limit,
            int windowSeconds) {

        String result = redisTemplate.execute(
                script,
                Arrays.asList(key),
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