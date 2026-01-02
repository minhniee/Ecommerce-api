package com.example.auth_shop.util;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * PaginationUtils - Utility class for handling pagination parameters
 * 
 * Provides validation and creation of Pageable objects with size limits
 * to prevent abuse and performance issues.
 * 
 * SECURITY NOTE:
 * Always use createPageable() methods with maxPageSize parameter to prevent
 * abuse. Users requesting very large page sizes (e.g., 1 million) could cause
 * performance issues or denial of service.
 */
public class PaginationUtils {

    /**
     * Default maximum page size if not configured
     */
    private static final int DEFAULT_MAX_PAGE_SIZE = 100;
    
    /**
     * Default page size if not specified
     */
    private static final int DEFAULT_PAGE_SIZE = 20;

    /**
     * Creates a Pageable object with validated and limited page size
     * 
     * @param page Page number (0-indexed)
     * @param size Requested page size (will be clamped to maxPageSize)
     * @param maxPageSize Maximum allowed page size
     * @param sortBy Field to sort by
     * @param sortDir Sort direction ("ASC" or "DESC")
     * @return Validated Pageable object
     */
    public static Pageable createPageable(int page, int size, int maxPageSize, String sortBy, String sortDir) {
        // Validate page number (must be >= 0)
        if (page < 0) {
            page = 0;
        }
        
        // Clamp size between 1 and maxPageSize
        int validatedSize = Math.max(1, Math.min(size, maxPageSize));
        
        // Create sort
        Sort sort = sortDir != null && sortDir.equalsIgnoreCase("DESC") 
                ? Sort.by(sortBy != null ? sortBy : "id").descending() 
                : Sort.by(sortBy != null ? sortBy : "id").ascending();
        
        return PageRequest.of(page, validatedSize, sort);
    }

    /**
     * Creates a Pageable object with default max page size (100)
     * 
     * @param page Page number (0-indexed)
     * @param size Requested page size (will be clamped to 100)
     * @param sortBy Field to sort by
     * @param sortDir Sort direction ("ASC" or "DESC")
     * @return Validated Pageable object
     */
    public static Pageable createPageable(int page, int size, String sortBy, String sortDir) {
        return createPageable(page, size, DEFAULT_MAX_PAGE_SIZE, sortBy, sortDir);
    }

    /**
     * Validates and clamps page size to maximum
     * 
     * @param size Requested page size
     * @param maxPageSize Maximum allowed page size
     * @return Validated size (clamped between 1 and maxPageSize)
     */
    public static int validatePageSize(int size, int maxPageSize) {
        return Math.max(1, Math.min(size, maxPageSize));
    }

    /**
     * Validates and clamps page size to default maximum (100)
     * 
     * @param size Requested page size
     * @return Validated size (clamped between 1 and 100)
     */
    public static int validatePageSize(int size) {
        return validatePageSize(size, DEFAULT_MAX_PAGE_SIZE);
    }

    /**
     * Gets the default maximum page size
     * 
     * @return Default maximum page size (100)
     */
    public static int getDefaultMaxPageSize() {
        return DEFAULT_MAX_PAGE_SIZE;
    }

    /**
     * Gets the default page size
     * 
     * @return Default page size (20)
     */
    public static int getDefaultPageSize() {
        return DEFAULT_PAGE_SIZE;
    }
}

