package com.zippyboot.infra.satoken.dao;

import cn.dev33.satoken.dao.SaTokenDao;
import cn.dev33.satoken.util.SaFoxUtil;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Sa-Token Redis 持久层实现
 *
 * @author lichunqing
 */
public class RedisSaTokenDao implements SaTokenDao {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ValueOperations<String, Object> valueOps;

    public RedisSaTokenDao(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.valueOps = redisTemplate.opsForValue();
    }

    // ==================== String 操作 ====================

    @Override
    public String get(String key) {
        Object val = valueOps.get(key);
        return val == null ? null : String.valueOf(val);
    }

    @Override
    public void set(String key, String value, long timeout) {
        if (isInvalidTimeout(timeout)) {
            return;
        }
        setValue(key, value, timeout);
    }

    @Override
    public void update(String key, String value) {
        long expire = getTimeout(key);
        if (expire == NOT_VALUE_EXPIRE) {
            return;
        }
        set(key, value, expire);
    }

    @Override
    public void delete(String key) {
        redisTemplate.delete(key);
    }

    @Override
    public long getTimeout(String key) {
        return resolveExpire(redisTemplate.getExpire(key));
    }

    @Override
    public void updateTimeout(String key, long timeout) {
        updateExpire(key, timeout, k -> get(k), (k, v, t) -> set(k, (String) v, t));
    }

    // ==================== Object 操作 ====================

    @Override
    public Object getObject(String key) {
        return valueOps.get(key);
    }

    @Override
    public void setObject(String key, Object object, long timeout) {
        if (isInvalidTimeout(timeout)) {
            return;
        }
        setValue(key, object, timeout);
    }

    @Override
    public void updateObject(String key, Object object) {
        long expire = getObjectTimeout(key);
        if (expire == NOT_VALUE_EXPIRE) {
            return;
        }
        setObject(key, object, expire);
    }

    @Override
    public void deleteObject(String key) {
        redisTemplate.delete(key);
    }

    @Override
    public long getObjectTimeout(String key) {
        return resolveExpire(redisTemplate.getExpire(key));
    }

    @Override
    public void updateObjectTimeout(String key, long timeout) {
        updateExpire(key, timeout, k -> getObject(k), (k, v, t) -> setObject(k, v, t));
    }

    // ==================== 搜索操作 ====================

    @Override
    public List<String> searchData(String prefix, String keyword, int start, int size, boolean sortType) {
        Set<String> keys = redisTemplate.keys(prefix + "*" + keyword + "*");
        if (keys == null || keys.isEmpty()) {
            return new ArrayList<>();
        }
        return SaFoxUtil.searchList(new ArrayList<>(keys), start, size, sortType);
    }

    // ==================== 私有方法 ====================

    private void setValue(String key, Object value, long timeout) {
        if (timeout == NEVER_EXPIRE) {
            valueOps.set(key, value);
        } else {
            valueOps.set(key, value, Duration.ofSeconds(timeout));
        }
    }

    private boolean isInvalidTimeout(long timeout) {
        return timeout == 0 || timeout <= NOT_VALUE_EXPIRE;
    }

    private long resolveExpire(Long expire) {
        if (expire == null) {
            return NOT_VALUE_EXPIRE;
        }
        return expire < 0 ? expire : expire;
    }

    @FunctionalInterface
    private interface ValueGetter {
        Object get(String key);
    }

    @FunctionalInterface
    private interface ValueSetter {
        void set(String key, Object value, long timeout);
    }

    private void updateExpire(String key, long timeout, ValueGetter getter, ValueSetter setter) {
        if (timeout == NEVER_EXPIRE) {
            long currentExpire = resolveExpire(redisTemplate.getExpire(key));
            if (currentExpire != NEVER_EXPIRE) {
                setter.set(key, getter.get(key), timeout);
            }
            return;
        }
        redisTemplate.expire(key, Duration.ofSeconds(timeout));
    }
}
