package com.izanami.management_shipping.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CacheConfig {

    @Value("${cache.ttl}")
    private Duration cacheTtl;

    @Value("${cache.expire}")
    private Long cacheExpire;

    @Bean
    public Caffeine<Object, Object> caffeineConfig() {
        return Caffeine.newBuilder()
                .maximumSize(cacheExpire)
                .expireAfterWrite(cacheTtl)
                .recordStats();
    }

    @Bean
    public CacheManager cacheManager(Caffeine<Object, Object> caffeine) {
        CaffeineCacheManager manager = new CaffeineCacheManager() {
            @Override
            protected org.springframework.cache.Cache adaptCaffeineCache(
                    String name,
                    com.github.benmanes.caffeine.cache.Cache<Object, Object> cache) {
                return new LoggingCaffeineCache(name, cache, isAllowNullValues());
            }
        };
        manager.setCaffeine(caffeine);
        return manager;
    }
}
