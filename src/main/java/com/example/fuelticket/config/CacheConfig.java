package com.example.fuelticket.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration pour activer Spring Cache
 */
@Configuration
@EnableCaching
public class CacheConfig {
    // La configuration est gérée via application.properties et ehcache.xml
}

