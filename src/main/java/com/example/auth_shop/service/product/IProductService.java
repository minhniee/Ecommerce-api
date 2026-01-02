package com.example.auth_shop.service.product;

import com.example.auth_shop.dto.ProductDto;
import com.example.auth_shop.model.Product;
import com.example.auth_shop.request.AddProductRequest;
import com.example.auth_shop.request.ProductUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface IProductService {

    Product addProduct(AddProductRequest reqs);
    Product getProductById(Long id);
    void deleteProduct(Long id);
    Product updateProduct(ProductUpdateRequest reqs, Long id);
    List<Product> getAllProducts();
    Page<Product> getAllProducts(Pageable pageable);
    List<Product> getProductsByCategory(String category);
    Page<Product> getProductsByCategory(String category, Pageable pageable);
    List<Product> getProductsByBrand(String brandName);
    Page<Product> getProductsByBrand(String brandName, Pageable pageable);
    List<Product> getProductsByCategoryAndBrand(String category, String brandName);
    Page<Product> getProductsByCategoryAndBrand(String category, String brandName, Pageable pageable);
    List<Product> getProductsByName(String productName);
    Page<Product> getProductsByName(String productName, Pageable pageable);
    List<Product> getProductsByBrandAndName(String brandName, String productName);
    Page<Product> getProductsByBrandAndName(String brandName, String productName, Pageable pageable);
    Long countProductsByBrandAndName(String brand, String productName);

    List<ProductDto> getConvertedProducts(List<Product> products);
    Page<ProductDto> getConvertedProducts(Page<Product> products);

    ProductDto convertToProductDto(Product product);
}
