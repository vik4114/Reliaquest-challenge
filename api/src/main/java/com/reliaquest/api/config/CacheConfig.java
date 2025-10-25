package com.reliaquest.api.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.List;

@Configuration
@EnableCaching
public class CacheConfig {

    private Caffeine<Object, Object> buildHighCapacityCache() {
        return Caffeine.newBuilder()
                .initialCapacity(1000)
                .maximumSize(50_000L)
                .recordStats();
    }
    private Caffeine<Object, Object> buildSingletonCache() {
        return Caffeine.newBuilder()
                .initialCapacity(1)
                .recordStats();
    }

    @Bean
    public CacheManager cacheManager() {
        // High-capacity caches
        CaffeineCache employeeById = new CaffeineCache(
                "employeeById",
                buildHighCapacityCache()
                        .expireAfterWrite(Duration.ofMinutes(60))
                        .build());

        CaffeineCache searchByName = new CaffeineCache(
                "searchByName",
                buildHighCapacityCache()
                        .expireAfterWrite(Duration.ofMinutes(30))
                        .build());

        // Singleton caches
        CaffeineCache employeesAll = new CaffeineCache(
                "employeesAll",
                buildSingletonCache()
                        .expireAfterWrite(Duration.ofMinutes(5))
                        .build());

        CaffeineCache highestSalary = new CaffeineCache(
                "highestSalary",
                buildSingletonCache()
                        .expireAfterWrite(Duration.ofMinutes(5))
                        .build());

        CaffeineCache topTenNamesBySalary = new CaffeineCache(
                "topTenNamesBySalary",
                buildSingletonCache()
                        .expireAfterWrite(Duration.ofMinutes(5))
                        .build());

        SimpleCacheManager cacheManager = new SimpleCacheManager();
        cacheManager.setCaches(List.of(
                employeeById,
                searchByName,
                employeesAll,
                highestSalary,
                topTenNamesBySalary
        ));

        return cacheManager;
    }
}

