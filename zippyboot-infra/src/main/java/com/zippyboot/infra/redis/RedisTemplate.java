package com.zippyboot.infra.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@ConditionalOnBean(StringRedisTemplate.class)
public class RedisTemplate {

    private static final DefaultRedisScript<Long> UNLOCK_SCRIPT = new DefaultRedisScript<>(
            """
                    if redis.call('get', KEYS[1]) == ARGV[1] then
                        return redis.call('del', KEYS[1])
                    end
                    return 0
                    """,
            Long.class
    );

    private final StringRedisTemplate redisTemplate;

    private org.springframework.data.redis.core.RedisTemplate<String, Object> objectRedisTemplate;

    @Autowired(required = false)
    public void setObjectRedisTemplate(org.springframework.data.redis.core.RedisTemplate<String, Object> objectRedisTemplate) {
        this.objectRedisTemplate = objectRedisTemplate;
    }

    public void put(String key, String value) {
        redisTemplate.opsForValue().set(key, value);
    }

    public void put(String key, String value, Duration ttl) {
        redisTemplate.opsForValue().set(key, value, ttl);
    }

    public void putAll(Map<String, String> values) {
        redisTemplate.opsForValue().multiSet(values);
    }

    public Boolean putIfAbsent(String key, String value) {
        return redisTemplate.opsForValue().setIfAbsent(key, value);
    }

    public Boolean putIfAbsent(String key, String value, Duration ttl) {
        return redisTemplate.opsForValue().setIfAbsent(key, value, ttl);
    }

    public Optional<String> get(String key) {
        return Optional.ofNullable(redisTemplate.opsForValue().get(key));
    }

    public Optional<String> getAndDelete(String key) {
        return Optional.ofNullable(redisTemplate.opsForValue().getAndDelete(key));
    }

    public List<String> multiGet(Collection<String> keys) {
        return redisTemplate.opsForValue().multiGet(keys);
    }

    public Optional<String> tryLock(String key, Duration ttl) {
        String token = UUID.randomUUID().toString();
        Boolean locked = putIfAbsent(key, token, ttl);
        return Boolean.TRUE.equals(locked) ? Optional.of(token) : Optional.empty();
    }

    public Boolean tryLock(String key, String token, Duration ttl) {
        return putIfAbsent(key, token, ttl);
    }

    public Boolean unlock(String key, String token) {
        Long deleted = redisTemplate.execute(UNLOCK_SCRIPT, List.of(key), token);
        return deleted != null && deleted > 0;
    }

    public List<Object> executePipelined(SessionCallback<?> sessionCallback) {
        return redisTemplate.executePipelined(sessionCallback);
    }

    public List<Object> executePipelined(RedisCallback<?> redisCallback) {
        return redisTemplate.executePipelined(redisCallback);
    }

    public Set<String> scan(String pattern) {
        return scan(ScanOptions.scanOptions().match(pattern).count(1000).build());
    }

    public Set<String> scan(String pattern, long count) {
        return scan(ScanOptions.scanOptions().match(pattern).count(count).build());
    }

    public Set<String> scan(ScanOptions scanOptions) {
        Set<String> keys = new LinkedHashSet<>();
        try (Cursor<String> cursor = redisTemplate.scan(scanOptions)) {
            while (cursor.hasNext()) {
                keys.add(cursor.next());
            }
        } catch (Exception e) {
            throw new IllegalStateException("Failed to scan redis keys", e);
        }
        return keys;
    }

    public Boolean exists(String key) {
        return redisTemplate.hasKey(key);
    }

    public Boolean expire(String key, Duration ttl) {
        return redisTemplate.expire(key, ttl);
    }

    public Optional<Duration> ttl(String key) {
        return Optional.ofNullable(redisTemplate.getExpire(key))
                .filter(seconds -> seconds >= 0)
                .map(Duration::ofSeconds);
    }

    public Long increment(String key) {
        return redisTemplate.opsForValue().increment(key);
    }

    public Long increment(String key, long delta) {
        return redisTemplate.opsForValue().increment(key, delta);
    }

    public Double increment(String key, double delta) {
        return redisTemplate.opsForValue().increment(key, delta);
    }

    public Long decrement(String key) {
        return redisTemplate.opsForValue().decrement(key);
    }

    public Long decrement(String key, long delta) {
        return redisTemplate.opsForValue().decrement(key, delta);
    }

    // ==================== Object 操作 ====================

    public void putObject(String key, Object value) {
        if (objectRedisTemplate == null) {
            throw new IllegalStateException("ObjectRedisTemplate is not available");
        }
        objectRedisTemplate.opsForValue().set(key, value);
    }

    public void putObject(String key, Object value, Duration ttl) {
        if (objectRedisTemplate == null) {
            throw new IllegalStateException("ObjectRedisTemplate is not available");
        }
        objectRedisTemplate.opsForValue().set(key, value, ttl);
    }

    public Object getObject(String key) {
        if (objectRedisTemplate == null) {
            throw new IllegalStateException("ObjectRedisTemplate is not available");
        }
        return objectRedisTemplate.opsForValue().get(key);
    }

    public void deleteObject(String key) {
        if (objectRedisTemplate == null) {
            throw new IllegalStateException("ObjectRedisTemplate is not available");
        }
        objectRedisTemplate.delete(key);
    }

    public Long getObjectExpire(String key) {
        if (objectRedisTemplate == null) {
            throw new IllegalStateException("ObjectRedisTemplate is not available");
        }
        return objectRedisTemplate.getExpire(key);
    }

    public Boolean objectExpire(String key, Duration ttl) {
        if (objectRedisTemplate == null) {
            throw new IllegalStateException("ObjectRedisTemplate is not available");
        }
        return objectRedisTemplate.expire(key, ttl);
    }

    public void hashPut(String key, String hashKey, String value) {
        redisTemplate.opsForHash().put(key, hashKey, value);
    }

    public void hashPutAll(String key, Map<String, String> values) {
        redisTemplate.opsForHash().putAll(key, values);
    }

    public Optional<Object> hashGet(String key, String hashKey) {
        return Optional.ofNullable(redisTemplate.opsForHash().get(key, hashKey));
    }

    public Map<Object, Object> hashEntries(String key) {
        return redisTemplate.opsForHash().entries(key);
    }

    public Boolean hashHasKey(String key, String hashKey) {
        return redisTemplate.opsForHash().hasKey(key, hashKey);
    }

    public Long hashDelete(String key, String... hashKeys) {
        return redisTemplate.opsForHash().delete(key, (Object[]) hashKeys);
    }

    public Long leftPush(String key, String value) {
        return redisTemplate.opsForList().leftPush(key, value);
    }

    public Long rightPush(String key, String value) {
        return redisTemplate.opsForList().rightPush(key, value);
    }

    public List<String> listRange(String key, long start, long end) {
        return redisTemplate.opsForList().range(key, start, end);
    }

    public Optional<String> leftPop(String key) {
        return Optional.ofNullable(redisTemplate.opsForList().leftPop(key));
    }

    public Optional<String> rightPop(String key) {
        return Optional.ofNullable(redisTemplate.opsForList().rightPop(key));
    }

    public Long addToSet(String key, String... values) {
        return redisTemplate.opsForSet().add(key, values);
    }

    public Set<String> members(String key) {
        return redisTemplate.opsForSet().members(key);
    }

    public Boolean isMember(String key, String value) {
        return redisTemplate.opsForSet().isMember(key, value);
    }

    public Long removeFromSet(String key, String... values) {
        return redisTemplate.opsForSet().remove(key, (Object[]) values);
    }

    public Boolean addToZSet(String key, String value, double score) {
        return redisTemplate.opsForZSet().add(key, value, score);
    }

    public Set<String> zSetRange(String key, long start, long end) {
        return redisTemplate.opsForZSet().range(key, start, end);
    }

    public Set<String> zSetRangeByScore(String key, double min, double max) {
        return redisTemplate.opsForZSet().rangeByScore(key, min, max);
    }

    public Optional<Double> zSetScore(String key, String value) {
        return Optional.ofNullable(redisTemplate.opsForZSet().score(key, value));
    }

    public Long removeFromZSet(String key, String... values) {
        return redisTemplate.opsForZSet().remove(key, (Object[]) values);
    }

    public Boolean delete(String key) {
        return redisTemplate.delete(key);
    }

    public Long delete(Collection<String> keys) {
        return redisTemplate.delete(keys);
    }
}
