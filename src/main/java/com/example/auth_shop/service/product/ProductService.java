package com.example.auth_shop.service.product;

import com.example.auth_shop.dto.ImageDto;
import com.example.auth_shop.dto.ProductDto;
import com.example.auth_shop.exceptions.AlreadyExistsException;
import com.example.auth_shop.exceptions.ProductNotFoundException;
import com.example.auth_shop.model.Category;
import com.example.auth_shop.model.Image;
import com.example.auth_shop.model.Product;
import com.example.auth_shop.repository.CategoryRepository;
import com.example.auth_shop.repository.ImageRepository;
import com.example.auth_shop.repository.ProductRepository;
import com.example.auth_shop.request.AddProductRequest;
import com.example.auth_shop.request.ProductUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductService implements IProductService {
    private final ProductRepository productRepo;
    private final CategoryRepository categoryRepository;
    private final ModelMapper modelMapper;
    private final ImageRepository imageRepository;

    @Override
    public Product addProduct(AddProductRequest reqs) {
        // check if category is exits in db
        // if yes ? save product ? save product with new category

        if (productExists(reqs.getName(), reqs.getBrand())) {
            throw new AlreadyExistsException(reqs.getBrand() + " " + reqs.getName() + " already exists, you may this product instead!");
        }

        Category category = Optional.ofNullable(categoryRepository.findByName(reqs.getCategory().getName()))
                .orElseGet(() -> {
                    Category newCategory = new Category(reqs.getCategory().getName());

                    return categoryRepository.save(newCategory);
                });
        reqs.setCategory(category);
        return productRepo.save(createProduct(reqs, category));
    }

    private boolean productExists(String name, String brand) {
        return productRepo.existsByNameAndBrand(name, brand);
    }

    private Product createProduct(AddProductRequest reqs, Category category) {
        return new Product(
                reqs.getName(),
                reqs.getBrand(),
                reqs.getPrice(),
                reqs.getInventory(),
                reqs.getDescription(),
                category
        );
    }

    @Override
    public Product getProductById(Long id) {
        return productRepo.findById(id).orElseThrow(() -> new ProductNotFoundException("Product not found"));
    }

    @Override
    public void deleteProduct(Long id) {
        productRepo.findById(id).ifPresentOrElse(productRepo::delete,
                () -> {
                    new ProductNotFoundException("Product not found");
                });
    }

    @Override
    public Product updateProduct(ProductUpdateRequest reqs, Long id) {
//
        return productRepo.findById(id)
                .map(existingProduct -> updateExistingProduct(existingProduct, reqs))
                .map(productRepo::save)
                .orElseThrow(() -> new ProductNotFoundException("Product not found"));
    }

    private Product updateExistingProduct(Product existingProduct, ProductUpdateRequest reqs) {
        existingProduct.setName(reqs.getName());
        existingProduct.setBrand(reqs.getBrand());
        existingProduct.setPrice(reqs.getPrice());
        existingProduct.setInventory(reqs.getInventory());
        existingProduct.setDescription(reqs.getDescription());

        Category category = categoryRepository.findByName(reqs.getCategory().getName());
        existingProduct.setCategory(category);
        return existingProduct;
    }

    @Override
    public List<Product> getAllProducts() {
        return productRepo.findAll();
    }

    @Override
    public Page<Product> getAllProducts(Pageable pageable) {
        return productRepo.findAll(pageable);
    }

    @Override
    public List<Product> getProductsByCategory(String category) {
        return productRepo.findByCategoryName(category);
    }

    @Override
    public Page<Product> getProductsByCategory(String category, Pageable pageable) {
        return productRepo.findByCategoryName(category, pageable);
    }

    @Override
    public List<Product> getProductsByBrand(String brandName) {
        return productRepo.findByBrand(brandName);
    }

    @Override
    public Page<Product> getProductsByBrand(String brandName, Pageable pageable) {
        return productRepo.findByBrand(brandName, pageable);
    }

    @Override
    public List<Product> getProductsByCategoryAndBrand(String category, String brandName) {
        return productRepo.findByCategoryNameAndBrand(category, brandName);
    }

    @Override
    public Page<Product> getProductsByCategoryAndBrand(String category, String brandName, Pageable pageable) {
        return productRepo.findByCategoryNameAndBrand(category, brandName, pageable);
    }

    @Override
    public List<Product> getProductsByName(String productName) {
        return productRepo.findByName(productName);
    }

    @Override
    public Page<Product> getProductsByName(String productName, Pageable pageable) {
        return productRepo.findByName(productName, pageable);
    }

    @Override
    public List<Product> getProductsByBrandAndName(String brandName, String productName) {
        return productRepo.findByBrandAndName(brandName, productName);
    }

    @Override
    public Page<Product> getProductsByBrandAndName(String brandName, String productName, Pageable pageable) {
        return productRepo.findByBrandAndName(brandName, productName, pageable);
    }

    @Override
    public Long countProductsByBrandAndName(String brand, String productName) {
        return productRepo.countByBrandAndName(brand, productName);
    }

    @Override
    public List<ProductDto> getConvertedProducts(List<Product> products) {
        return products.stream().map(this::convertToProductDto).toList();
    }

    @Override
    public Page<ProductDto> getConvertedProducts(Page<Product> products) {
        return products.map(this::convertToProductDto);
    }

    @Override
    public ProductDto convertToProductDto(Product product) {
        ProductDto productDto = modelMapper.map(product, ProductDto.class);
        List<Image> images = imageRepository.findByProductId(product.getId());
        List<ImageDto> imageDtos = images.stream()
                .map(image -> modelMapper.map(image, ImageDto.class))
                .toList();
        productDto.setImages(imageDtos);
        return productDto;
    }
}
