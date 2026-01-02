package com.example.auth_shop.repository;

import com.example.auth_shop.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByCategoryName(String category);
    Page<Product> findByCategoryName(String category, Pageable pageable);

    List<Product> findByBrand(String brandName);
    Page<Product> findByBrand(String brandName, Pageable pageable);

    List<Product> findByCategoryNameAndBrand(String category, String brandName);
    Page<Product> findByCategoryNameAndBrand(String category, String brandName, Pageable pageable);

    List<Product> findByName(String productName);
    Page<Product> findByName(String productName, Pageable pageable);

    List<Product> findByBrandAndName(String brandName, String productName);
    Page<Product> findByBrandAndName(String brandName, String productName, Pageable pageable);

    Long countByBrandAndName(String brand, String productName);

    Product getProductById(Long productId);

    boolean existsByNameAndBrand(String name, String brand);
}
