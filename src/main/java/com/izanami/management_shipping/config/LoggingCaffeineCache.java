package com.izanami.management_shipping.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.caffeine.CaffeineCache;

@Slf4j
public class LoggingCaffeineCache extends CaffeineCache {

    public LoggingCaffeineCache(String name,
                                com.github.benmanes.caffeine.cache.Cache<Object, Object> cache,
                                boolean allowNullValues) {
        super(name, cache, allowNullValues);
    }

    @Override
    protected Object lookup(Object key) {
        Object value = super.lookup(key);
        if (log.isDebugEnabled()) {
            if (value != null) {
                log.debug("Cache HIT  [{}] key={}", getName(), key);
            } else {
                log.debug("Cache MISS [{}] key={}", getName(), key);
            }
        }
        return value;
    }
}
