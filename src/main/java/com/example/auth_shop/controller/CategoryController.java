package com.example.auth_shop.controller;

import com.example.auth_shop.config.PaginationConfig;
import com.example.auth_shop.model.Category;
import com.example.auth_shop.response.APIResponse;
import com.example.auth_shop.response.PaginatedResponse;
import com.example.auth_shop.service.category.ICategoryService;
import com.example.auth_shop.util.PaginationUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${api.prefix}/categories")
@RequiredArgsConstructor
public class CategoryController {
    private final ICategoryService categoryService;
    private final PaginationConfig paginationConfig;

    @GetMapping
    public ResponseEntity<APIResponse> getAllCategories(
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "20") int size,
            @RequestParam(required = false, defaultValue = "id") String sortBy,
            @RequestParam(required = false, defaultValue = "ASC") String sortDir) {
        
        Pageable pageable = PaginationUtils.createPageable(
                page, size, paginationConfig.getMaxPageSize(), sortBy, sortDir);
        
        Page<Category> categoriesPage = categoryService.getAllCategories(pageable);
        PaginatedResponse<Category> paginatedResponse = PaginatedResponse.of(categoriesPage);
        
        return ResponseEntity.ok(APIResponse.success("Categories retrieved successfully", paginatedResponse));
    }

    @GetMapping("/{id}")
    public ResponseEntity<APIResponse> getCategoryById(@PathVariable Long id) {
        Category category = categoryService.getCategoryById(id);
        return ResponseEntity.ok(APIResponse.success("Category retrieved successfully", category));
    }

    @GetMapping("/name/{name}")
    public ResponseEntity<APIResponse> getCategoryByName(@PathVariable String name) {
        Category category = categoryService.getCategoryByName(name);
        return ResponseEntity.ok(APIResponse.success("Category retrieved successfully", category));
    }

    @PostMapping
    public ResponseEntity<APIResponse> addCategory(@RequestBody Category category) {
        Category newCategory = categoryService.addCategory(category);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(APIResponse.created("Category created successfully", newCategory));
    }

    @PutMapping("/{id}")
    public ResponseEntity<APIResponse> updateCategory(
            @RequestBody Category category, 
            @PathVariable Long id) {
        Category updatedCategory = categoryService.updateCategory(category, id);
        return ResponseEntity.ok(APIResponse.success("Category updated successfully", updatedCategory));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<APIResponse> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategoryById(id);
        return ResponseEntity.ok(APIResponse.success("Category deleted successfully"));
    }
}
