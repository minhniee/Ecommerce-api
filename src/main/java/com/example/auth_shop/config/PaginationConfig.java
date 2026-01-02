package com.example.auth_shop.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * PaginationConfig - Configuration for pagination settings
 * 
 * Reads pagination configuration from application.properties
 */
@Component
@Getter
public class PaginationConfig {

    /**
     * Maximum allowed page size to prevent abuse
     * Default: 100
     * Can be configured via: api.pagination.max-page-size
     */
    @Value("${api.pagination.max-page-size:100}")
    private int maxPageSize;

    /**
     * Default page size when not specified
     * Default: 20
     * Can be configured via: api.pagination.default-page-size
     */
    @Value("${api.pagination.default-page-size:20}")
    private int defaultPageSize;
}



