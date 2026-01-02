package com.example.auth_shop.controller;

import com.example.auth_shop.config.PaginationConfig;
import com.example.auth_shop.dto.ProductDto;
import com.example.auth_shop.model.Product;
import com.example.auth_shop.request.AddProductRequest;
import com.example.auth_shop.request.ProductUpdateRequest;
import com.example.auth_shop.response.APIResponse;
import com.example.auth_shop.response.PaginatedResponse;
import com.example.auth_shop.service.product.IProductService;
import com.example.auth_shop.util.PaginationUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("${api.prefix}/products")
public class ProductController {
    private final IProductService productService;
    private final PaginationConfig paginationConfig;

    @GetMapping
    public ResponseEntity<APIResponse> getAllProducts(
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "20") int size,
            @RequestParam(required = false, defaultValue = "id") String sortBy,
            @RequestParam(required = false, defaultValue = "ASC") String sortDir) {
        
        Pageable pageable = PaginationUtils.createPageable(
                page, size, paginationConfig.getMaxPageSize(), sortBy, sortDir);
        
        Page<Product> productsPage = productService.getAllProducts(pageable);
        Page<ProductDto> productDtosPage = productService.getConvertedProducts(productsPage);
        PaginatedResponse<ProductDto> paginatedResponse = PaginatedResponse.of(productDtosPage);
        
        return ResponseEntity.ok(APIResponse.success("Products retrieved successfully", paginatedResponse));
    }

    @GetMapping("/{id}")
    public ResponseEntity<APIResponse> getProductById(@PathVariable Long id) {
        Product product = productService.getProductById(id);
        ProductDto convertedProduct = productService.convertToProductDto(product);
        return ResponseEntity.ok(APIResponse.success("Product retrieved successfully", convertedProduct));
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping
    public ResponseEntity<APIResponse> addProduct(@Valid @RequestBody AddProductRequest request) {
        Product product = productService.addProduct(request);
        ProductDto convertedProduct = productService.convertToProductDto(product);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(APIResponse.created("Product created successfully", convertedProduct));
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<APIResponse> updateProduct(
            @Valid @RequestBody ProductUpdateRequest request, 
            @PathVariable Long id) {
        Product product = productService.updateProduct(request, id);
        ProductDto convertedProduct = productService.convertToProductDto(product);
        return ResponseEntity.ok(APIResponse.success("Product updated successfully", convertedProduct));
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<APIResponse> deleteProductById(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok(APIResponse.success("Product deleted successfully"));
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<APIResponse> getProductsByCategory(
            @PathVariable String category,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "20") int size,
            @RequestParam(required = false, defaultValue = "id") String sortBy,
            @RequestParam(required = false, defaultValue = "ASC") String sortDir) {
        
        Pageable pageable = PaginationUtils.createPageable(
                page, size, paginationConfig.getMaxPageSize(), sortBy, sortDir);
        
        Page<Product> productsPage = productService.getProductsByCategory(category, pageable);
        Page<ProductDto> productDtosPage = productService.getConvertedProducts(productsPage);
        PaginatedResponse<ProductDto> paginatedResponse = PaginatedResponse.of(productDtosPage);
        
        return ResponseEntity.ok(APIResponse.success("Products retrieved successfully", paginatedResponse));
    }

    @GetMapping("/brand/{brand}")
    public ResponseEntity<APIResponse> getProductsByBrand(
            @PathVariable String brand,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "20") int size,
            @RequestParam(required = false, defaultValue = "id") String sortBy,
            @RequestParam(required = false, defaultValue = "ASC") String sortDir) {
        
        Pageable pageable = PaginationUtils.createPageable(
                page, size, paginationConfig.getMaxPageSize(), sortBy, sortDir);
        
        Page<Product> productsPage = productService.getProductsByBrand(brand, pageable);
        Page<ProductDto> productDtosPage = productService.getConvertedProducts(productsPage);
        PaginatedResponse<ProductDto> paginatedResponse = PaginatedResponse.of(productDtosPage);
        
        return ResponseEntity.ok(APIResponse.success("Products retrieved successfully", paginatedResponse));
    }

    @GetMapping("/search")
    public ResponseEntity<APIResponse> searchProducts(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) String name,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "20") int size,
            @RequestParam(required = false, defaultValue = "id") String sortBy,
            @RequestParam(required = false, defaultValue = "ASC") String sortDir) {
        
        Pageable pageable = PaginationUtils.createPageable(
                page, size, paginationConfig.getMaxPageSize(), sortBy, sortDir);
        
        Page<Product> productsPage = determineSearchStrategy(category, brand, name, pageable);
        Page<ProductDto> productDtosPage = productService.getConvertedProducts(productsPage);
        PaginatedResponse<ProductDto> paginatedResponse = PaginatedResponse.of(productDtosPage);
        
        return ResponseEntity.ok(APIResponse.success("Products retrieved successfully", paginatedResponse));
    }
    
    /**
     * Determines which search method to use based on the provided criteria
     */
    private Page<Product> determineSearchStrategy(String category, String brand, String name, Pageable pageable) {
        if (category != null && brand != null) {
            return productService.getProductsByCategoryAndBrand(category, brand, pageable);
        }
        if (brand != null && name != null) {
            return productService.getProductsByBrandAndName(brand, name, pageable);
        }
        if (name != null) {
            return productService.getProductsByName(name, pageable);
        }
        if (brand != null) {
            return productService.getProductsByBrand(brand, pageable);
        }
        if (category != null) {
            return productService.getProductsByCategory(category, pageable);
        }
        return productService.getAllProducts(pageable);
    }
}
