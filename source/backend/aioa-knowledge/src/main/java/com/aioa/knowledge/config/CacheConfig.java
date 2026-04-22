package com.aioa.knowledge.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * 缓存配置 - L1(Local Caffeine) + L2(Redis) 二级缓存
 */
@Configuration
@EnableCaching
public class CacheConfig {
    
    /**
     * L1缓存：Caffeine本地缓存（高性能）
     */
    @Bean("caffeineCacheManager")
    public CacheManager caffeineCacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .initialCapacity(100) // 初始容量
                .maximumSize(1000)    // 最大容量
                .expireAfterWrite(Duration.ofMinutes(10)) // 写入后10分钟过期
                .expireAfterAccess(Duration.ofMinutes(5)) // 访问后5分钟过期
                .recordStats());      // 记录统计信息
        
        // 设置缓存名称
        cacheManager.setCacheNames(java.util.Arrays.asList(
            "knowledge:docs",      // 文档缓存
            "knowledge:search",    // 搜索结果缓存
            "knowledge:stats",     // 统计信息缓存
            "knowledge:categories" // 分类缓存
        ));
        
        return cacheManager;
    }
    
    /**
     * L2缓存：Redis分布式缓存（一致性）
     */
    @Bean("redisCacheManager")
    public CacheManager redisCacheManager(RedisConnectionFactory redisConnectionFactory) {
        // 默认缓存配置
        RedisCacheConfiguration defaultCacheConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(30)) // 默认30分钟过期
                .disableCachingNullValues()       // 不缓存null值
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()));
        
        // 针对不同缓存的特定配置
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
        
        // 文档缓存：1小时
        cacheConfigurations.put("knowledge:docs", 
            RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofHours(1))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer())));
        
        // 搜索结果缓存：15分钟（搜索频率高，但需要保持新鲜度）
        cacheConfigurations.put("knowledge:search",
            RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(15))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer())));
        
        // 统计信息缓存：5分钟（需要实时性）
        cacheConfigurations.put("knowledge:stats",
            RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(5))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer())));
        
        // 分类缓存：1天（不常变化）
        cacheConfigurations.put("knowledge:categories",
            RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofDays(1))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer())));
        
        return RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(defaultCacheConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .transactionAware()
                .build();
    }
    
    /**
     * 二级缓存管理器：L1 + L2组合
     */
    @Bean
    public CacheManager compositeCacheManager(
            CacheManager caffeineCacheManager,
            CacheManager redisCacheManager) {
        
        return new CompositeCacheManager(caffeineCacheManager, redisCacheManager);
    }
    
    /**
     * 复合缓存管理器实现
     */
    public static class CompositeCacheManager implements CacheManager {
        private final CacheManager l1CacheManager; // Caffeine
        private final CacheManager l2CacheManager; // Redis
        
        public CompositeCacheManager(CacheManager l1CacheManager, CacheManager l2CacheManager) {
            this.l1CacheManager = l1CacheManager;
            this.l2CacheManager = l2CacheManager;
        }
        
        @Override
        public org.springframework.cache.Cache getCache(String name) {
            return new CompositeCache(
                l1CacheManager.getCache(name),
                l2CacheManager.getCache(name)
            );
        }
        
        @Override
        public java.util.Collection<String> getCacheNames() {
            java.util.Set<String> names = new java.util.HashSet<>();
            names.addAll(l1CacheManager.getCacheNames());
            names.addAll(l2CacheManager.getCacheNames());
            return names;
        }
    }
    
    /**
     * 复合缓存实现
     */
    public static class CompositeCache implements org.springframework.cache.Cache {
        private final org.springframework.cache.Cache l1Cache;
        private final org.springframework.cache.Cache l2Cache;
        private final String name;
        
        public CompositeCache(org.springframework.cache.Cache l1Cache, org.springframework.cache.Cache l2Cache) {
            this.l1Cache = l1Cache;
            this.l2Cache = l2Cache;
            this.name = l1Cache != null ? l1Cache.getName() : l2Cache.getName();
        }
        
        @Override
        public String getName() {
            return name;
        }
        
        @Override
        public Object getNativeCache() {
            return this;
        }
        
        @Override
        public ValueWrapper get(Object key) {
            // 先从L1缓存获取
            ValueWrapper value = l1Cache != null ? l1Cache.get(key) : null;
            if (value != null) {
                return value;
            }
            
            // L1未命中，从L2获取
            value = l2Cache != null ? l2Cache.get(key) : null;
            if (value != null && l1Cache != null) {
                // 将L2的值写入L1缓存
                l1Cache.put(key, value.get());
            }
            
            return value;
        }
        
        @Override
        public <T> T get(Object key, Class<T> type) {
            // 先从L1缓存获取
            T value = l1Cache != null ? l1Cache.get(key, type) : null;
            if (value != null) {
                return value;
            }
            
            // L1未命中，从L2获取
            value = l2Cache != null ? l2Cache.get(key, type) : null;
            if (value != null && l1Cache != null) {
                // 将L2的值写入L1缓存
                l1Cache.put(key, value);
            }
            
            return value;
        }
        
        @Override
        public void put(Object key, Object value) {
            // 同时写入L1和L2缓存
            if (l1Cache != null) {
                l1Cache.put(key, value);
            }
            if (l2Cache != null) {
                l2Cache.put(key, value);
            }
        }
        
        @Override
        public void evict(Object key) {
            // 同时从L1和L2缓存删除
            if (l1Cache != null) {
                l1Cache.evict(key);
            }
            if (l2Cache != null) {
                l2Cache.evict(key);
            }
        }
        
        @Override
        public void clear() {
            // 同时清空L1和L2缓存
            if (l1Cache != null) {
                l1Cache.clear();
            }
            if (l2Cache != null) {
                l2Cache.clear();
            }
        }
        
        @Override
        public ValueWrapper putIfAbsent(Object key, Object value) {
            // 先检查L2缓存
            ValueWrapper existing = l2Cache != null ? l2Cache.get(key) : null;
            if (existing == null) {
                // L2中没有，写入两个缓存
                if (l2Cache != null) {
                    existing = l2Cache.putIfAbsent(key, value);
                }
                if (existing == null && l1Cache != null) {
                    l1Cache.put(key, value);
                }
            } else if (l1Cache != null) {
                // L2中有，同步到L1
                l1Cache.put(key, existing.get());
            }
            
            return existing;
        }
    }
}