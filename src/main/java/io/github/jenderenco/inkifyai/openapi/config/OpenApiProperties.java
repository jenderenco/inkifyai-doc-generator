package io.github.jenderenco.inkifyai.openapi.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "openapi.fetcher")
public record OpenApiProperties(
    int connectTimeout,
    int readTimeout,
    int maxRetries,
    long retryDelay,
    boolean validateUrl,
    int cacheSize,
    long cacheTtl) {}
