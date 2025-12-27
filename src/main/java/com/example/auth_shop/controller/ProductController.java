package com.example.auth_shop.controller;

import com.example.auth_shop.security.config.GlobalVariable;
import com.example.auth_shop.dto.ProductDto;
import com.example.auth_shop.exceptions.AlreadyExistsException;
import com.example.auth_shop.exceptions.ResourceNotFoundException;
import com.example.auth_shop.model.Product;
import com.example.auth_shop.request.AddProductRequest;
import com.example.auth_shop.request.ProductUpdateRequest;
import com.example.auth_shop.response.APIResponse;
import com.example.auth_shop.service.product.IProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.HttpStatus.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("${api.prefix}/products")
public class ProductController {
    private final IProductService productService;

    @GetMapping("/all")
    public ResponseEntity<APIResponse> getAllProducts() {
        try {
            List<Product> products = productService.getAllProducts();
            if (products.isEmpty()) {
                return ResponseEntity.status(NOT_FOUND).body(new APIResponse("No products found", null));
            }
            List<ProductDto> convertedProducts = productService.getConvertedProducts(products);
            return ResponseEntity.ok(new APIResponse("Found", convertedProducts));
        } catch (Exception e) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR).body(new APIResponse(e.getMessage(), null));
        }
    }

    @GetMapping("/product/{id}")
    public ResponseEntity<APIResponse> getProductById(@PathVariable Long id) {
        try {
            Product product = productService.getProductById(id);
            ProductDto convertedProduct = productService.convertToProductDto(product);
            return ResponseEntity.ok(new APIResponse("Found", convertedProduct));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(NOT_FOUND).body(new APIResponse(e.getMessage(), null));

        }
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    @PostMapping("/add")
    public ResponseEntity<APIResponse> addProduct(@Valid @RequestBody AddProductRequest reqs) {
        try {
            Product product = productService.addProduct(reqs);
            ProductDto convertedProduct = productService.convertToProductDto(product);
            return ResponseEntity.ok(new APIResponse("Added", convertedProduct));
        }catch (AlreadyExistsException e) {
            return ResponseEntity.status(CONFLICT).body(new APIResponse(GlobalVariable.getCurrentDateTime(), e.getMessage(), null));
        }catch ( RuntimeException e) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR).body(new APIResponse(GlobalVariable.getCurrentDateTime(), e.getMessage(), null));

        }
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    @DeleteMapping("/product/{id}/delete")
    public ResponseEntity<APIResponse> deleteProductById(@PathVariable Long id) {
        try {
            productService.deleteProduct(id);
            return ResponseEntity.ok(new APIResponse("Delete product success!!", null));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(NOT_FOUND).body(new APIResponse(e.getMessage(), null));
        }
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    @PutMapping("/product/{id}/update")
    public ResponseEntity<APIResponse> updateProduct(@Valid @RequestBody ProductUpdateRequest reqs, @PathVariable Long id) {
        try {
            Product product = productService.updateProduct(reqs, id);
            ProductDto convertedProduct = productService.convertToProductDto(product);

            return ResponseEntity.ok(new APIResponse("Updated product success!", convertedProduct));
        } catch (RuntimeException e) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR).body(new APIResponse(e.getMessage(), null));
        }
    }

    @GetMapping("product/{category}/all/products")
    public ResponseEntity<APIResponse> getProductByCategory(@PathVariable String category) {
        try {
            List<Product> products = productService.getProductsByCategory(category);

            if (products.isEmpty()) {
                return ResponseEntity.status(NOT_FOUND).body(new APIResponse("No products found", null));
            }
            List<ProductDto> convertedProducts = productService.getConvertedProducts(products);
            return ResponseEntity.ok(new APIResponse("Success", convertedProducts));
        } catch (RuntimeException e) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR).body(new APIResponse(e.getMessage(), null));
        }
    }

    @GetMapping("product/by/brand")
    public ResponseEntity<APIResponse> getProductByBrand(@RequestParam String brand) {
        try {
            List<Product> products = productService.getProductsByBrand(brand);

            if (products.isEmpty()) {
                return ResponseEntity.status(NOT_FOUND).body(new APIResponse("No products found", null));
            }
            List<ProductDto> convertedProducts = productService.getConvertedProducts(products);
            return ResponseEntity.ok(new APIResponse("Success", convertedProducts));
        } catch (RuntimeException e) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR).body(new APIResponse(e.getMessage(), null));
        }
    }

    @GetMapping("product/by/cat-and-brand")
    public ResponseEntity<APIResponse> getProductByCategoryAndBrand(@RequestParam String category, @RequestParam String brand) {

        try {
            List<Product> products = productService.getProductsByCategoryAndBrand(category, brand);

            if (products.isEmpty()) {
                return ResponseEntity.status(NOT_FOUND).body(new APIResponse("No products found", null));
            }
            List<ProductDto> convertedProducts = productService.getConvertedProducts(products);
            return ResponseEntity.ok(new APIResponse("Success", convertedProducts));
        } catch (RuntimeException e) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR).body(new APIResponse(e.getMessage(), null));
        }
    }

    @GetMapping("product/by/name")
    public ResponseEntity<APIResponse> getProductByName(@RequestParam String name) {
        try {
            List<Product> products = productService.getProductsByName(name);

            if (products.isEmpty()) {
                return ResponseEntity.status(NOT_FOUND).body(new APIResponse("No products found", null));
            }
            List<ProductDto> convertedProducts = productService.getConvertedProducts(products);
            return ResponseEntity.ok(new APIResponse("Success", convertedProducts));
        } catch (RuntimeException e) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR).body(new APIResponse(e.getMessage(), null));
        }
    }

    @GetMapping("product/by/brand-and-name")
    public ResponseEntity<APIResponse> getProductByBrandAndName(@RequestParam String brand, @RequestParam String name) {
        try {
            List<Product> products = productService.getProductsByBrandAndName(brand, name);
            if (products.isEmpty()) {
                return ResponseEntity.status(NOT_FOUND).body(new APIResponse("No products found", null));
            }
            List<ProductDto> convertedProducts = productService.getConvertedProducts(products);
            return ResponseEntity.ok(new APIResponse("Success", convertedProducts));
        } catch (RuntimeException e) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR).body(new APIResponse(e.getMessage(), null));
        }
    }


}
