package com.example.auth_shop.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * PaginationParams - Request parameters for pagination
 * 
 * Encapsulates pagination request parameters to reduce duplication
 * and provide consistent pagination handling across controllers.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaginationParams {
    
    /**
     * Page number (0-indexed)
     */
    private int page = 0;
    
    /**
     * Page size (number of elements per page)
     */
    private int size = 20;
    
    /**
     * Field name to sort by
     */
    private String sortBy = "id";
    
    /**
     * Sort direction: "ASC" or "DESC"
     */
    private String sortDir = "ASC";
    
    /**
     * Creates PaginationParams with default values
     */
    public static PaginationParams defaults() {
        return new PaginationParams();
    }
    
    /**
     * Creates PaginationParams with custom values
     */
    public static PaginationParams of(int page, int size, String sortBy, String sortDir) {
        return new PaginationParams(page, size, sortBy, sortDir);
    }
    
    /**
     * Creates PaginationParams with custom sort field and direction
     */
    public static PaginationParams withSort(String sortBy, String sortDir) {
        return new PaginationParams(0, 20, sortBy, sortDir);
    }
}

