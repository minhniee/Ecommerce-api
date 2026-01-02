package com.example.auth_shop.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * PaginatedResponse - Wrapper class for paginated API responses
 * 
 * This class provides pagination metadata along with the data.
 * 
 * EXAMPLE:
 * {
 *   "content": [...],
 *   "page": 0,
 *   "size": 20,
 *   "totalElements": 150,
 *   "totalPages": 8,
 *   "first": true,
 *   "last": false,
 *   "numberOfElements": 20
 * }
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PaginatedResponse<T> {
    
    /**
     * The actual data/content for the current page
     */
    private List<T> content;
    
    /**
     * Current page number (0-indexed)
     */
    private int page;
    
    /**
     * Size of the page (number of elements per page)
     */
    private int size;
    
    /**
     * Total number of elements across all pages
     */
    private long totalElements;
    
    /**
     * Total number of pages
     */
    private int totalPages;
    
    /**
     * Whether this is the first page
     */
    private boolean first;
    
    /**
     * Whether this is the last page
     */
    private boolean last;
    
    /**
     * Number of elements in the current page
     */
    private int numberOfElements;
    
    /**
     * Whether the page has content
     */
    private boolean empty;
    
    /**
     * Factory method to create PaginatedResponse from Spring Data Page
     */
    public static <T> PaginatedResponse<T> of(org.springframework.data.domain.Page<T> page) {
        return PaginatedResponse.<T>builder()
                .content(page.getContent())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .numberOfElements(page.getNumberOfElements())
                .empty(page.isEmpty())
                .build();
    }
    
    /**
     * Factory method to create PaginatedResponse with custom content
     */
    public static <T> PaginatedResponse<T> of(List<T> content, int page, int size, long totalElements) {
        int totalPages = (int) Math.ceil((double) totalElements / size);
        return PaginatedResponse.<T>builder()
                .content(content)
                .page(page)
                .size(size)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .first(page == 0)
                .last(page >= totalPages - 1)
                .numberOfElements(content.size())
                .empty(content.isEmpty())
                .build();
    }
}



