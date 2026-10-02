package com.rinas.revenue.cache;

import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * Redis-backed analytics cache.
 *
 * <p>The cache is best-effort: if Redis is unreachable the application must keep
 * serving correct results from PostgreSQL, just more slowly. Every operation is
 * therefore wrapped so that a Redis failure degrades to a miss rather than a
 * 500. A cache that can take down its application is worse than no cache.
 *
 * <p>Keys are versioned ({@code v1}) so a change to a response shape can be
 * rolled out by bumping the version instead of flushing the database.
 */
@Component
public class RedisAnalyticsCache implements AnalyticsCache {

    private static final Logger log = LoggerFactory.getLogger(RedisAnalyticsCache.class);
    private static final String PREFIX = "ri:analytics:v1:";

    private final StringRedisTemplate redis;

    public RedisAnalyticsCache(StringRedisTemplate redis) {
        this.redis = redis;
    }

    @Override
    public Optional<String> get(UUID businessId, String logicalKey) {
        try {
            return Optional.ofNullable(redis.opsForValue().get(physicalKey(businessId, logicalKey)));
        } catch (RuntimeException ex) {
            log.debug("Cache read failed, treating as a miss: {}", ex.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public void put(UUID businessId, String logicalKey, String json, long ttlSeconds) {
        try {
            redis.opsForValue().set(physicalKey(businessId, logicalKey), json,
                java.time.Duration.ofSeconds(ttlSeconds));
        } catch (RuntimeException ex) {
            log.debug("Cache write failed, continuing without caching: {}", ex.getMessage());
        }
    }

    @Override
    public void invalidateBusiness(UUID businessId) {
        String pattern = PREFIX + businessId + ":*";
        try (Cursor<String> cursor = redis.scan(
                ScanOptions.scanOptions().match(pattern).count(200).build())) {
            while (cursor.hasNext()) {
                redis.unlink(cursor.next());
            }
        } catch (RuntimeException ex) {
            log.debug("Cache invalidation failed: {}", ex.getMessage());
        }
    }

    private static String physicalKey(UUID businessId, String logicalKey) {
        return PREFIX + businessId + ":" + logicalKey;
    }
}
