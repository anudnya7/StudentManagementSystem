package org.example.studentmanagementsystem.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.context.annotation.Configuration;

/**
 * Switches Spring's caching on and makes the app survive a Redis outage.
 * Without the error handler, a stopped Redis would turn every cached API call into a 500 error.
 * With it, we log a warning and read from PostgreSQL instead.
 */
@Slf4j
@Configuration
@EnableCaching
public class CacheConfig implements CachingConfigurer {

    @Override
    public CacheErrorHandler errorHandler() {
        return new CacheErrorHandler() {
            @Override
            public void handleCacheGetError(RuntimeException ex, Cache cache, Object key) {
                log.warn("Cache GET failed on '{}' key {}: {}", cache.getName(), key, ex.getMessage());
            }

            @Override
            public void handleCachePutError(RuntimeException ex, Cache cache, Object key, Object value) {
                log.warn("Cache PUT failed on '{}' key {}: {}", cache.getName(), key, ex.getMessage());
            }

            @Override
            public void handleCacheEvictError(RuntimeException ex, Cache cache, Object key) {
                log.warn("Cache EVICT failed on '{}' key {}: {}", cache.getName(), key, ex.getMessage());
            }

            @Override
            public void handleCacheClearError(RuntimeException ex, Cache cache) {
                log.warn("Cache CLEAR failed on '{}': {}", cache.getName(), ex.getMessage());
            }
        };
    }
}