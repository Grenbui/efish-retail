package com.retail.storeback.category.service;

import com.retail.storeback.category.model.Category;
import com.retail.storeback.category.model.Product;
import com.retail.storeback.category.model.dto.ProductRequest;
import com.retail.storeback.category.model.dto.ProductResponse;
import com.retail.storeback.category.model.mapper.ProductMapper;
import com.retail.storeback.category.repository.CategoryRepository;
import com.retail.storeback.category.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductMapper productMapper;

    @Transactional(readOnly = true)
    public ProductResponse getProductById(UUID id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Product not found with id: " + id));

        return productMapper.toResponse(product);
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll().stream()
                .map(productMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> getProductsByCategory(UUID categoryId) {
        return productRepository.findByCategoryId(categoryId).stream()
                .map(productMapper::toResponse)
                .toList();
    }

    @Transactional
    public ProductResponse create(ProductRequest productRequest) {
        if (productRequest.sku() != null && productRepository.existsBySku(productRequest.sku())) {
            throw new IllegalArgumentException("Product with SKU already exists: " + productRequest.sku());
        }

        Category category = categoryRepository.findById(productRequest.categoryId())
                .orElseThrow(() -> new EntityNotFoundException("Category not found with id: " + productRequest.categoryId()));

        Product product = productMapper.toEntity(productRequest);
        product.setCategory(category);

        Product savedProduct = productRepository.save(product);
        return productMapper.toResponse(savedProduct);
    }

    @Transactional
    public ProductResponse update(ProductRequest productRequest, UUID id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Product not found with id: " + id));

        if (productRequest.sku() != null && !productRequest.sku().equals(product.getSku())) {
            if (productRepository.existsBySku(productRequest.sku())) {
                throw new IllegalArgumentException("Product with SKU already exists: " + productRequest.sku());
            }
            product.setSku(productRequest.sku());
        }

        if (productRequest.categoryId() != null &&
                (product.getCategory() == null || !productRequest.categoryId().equals(product.getCategory().getId()))) {
            Category category = categoryRepository.findById(productRequest.categoryId())
                    .orElseThrow(() -> new EntityNotFoundException("Category not found with id: " + productRequest.categoryId()));
            product.setCategory(category);
        }

        productMapper.updateEntityFromRequest(productRequest, product);

        Product updatedProduct = productRepository.save(product);
        return productMapper.toResponse(updatedProduct);
    }

    @Transactional
    public String delete(UUID id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Product not found with id: " + id));

        productRepository.delete(product);

        return "Product deleted successfully";
    }
}
