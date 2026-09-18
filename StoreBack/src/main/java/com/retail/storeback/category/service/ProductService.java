package com.retail.storeback.category.service;

import com.retail.storeback.category.model.Category;
import com.retail.storeback.category.model.Product;
import com.retail.storeback.category.model.dto.ProductRequest;
import com.retail.storeback.category.model.dto.ProductResponse;
import com.retail.storeback.category.model.mapper.ProductMapper;
import com.retail.storeback.category.repository.CategoryRepository;
import com.retail.storeback.category.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductMapper productMapper;

    public ProductResponse getProductById(UUID id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));

        return  productMapper.toResponse(product);
    }

    public List<ProductResponse> getAllProducts() {
        List<Product> products = productRepository.findAll();
        List<ProductResponse> productResponses = new ArrayList<>();
        for (Product product : products) {
            productResponses.add(productMapper.toResponse(product));
        }

        return productResponses;
    }

    public ProductResponse create(ProductRequest productRequest) {
        Product product = productMapper.toEntity(productRequest);
        productRepository.save(product);
        return productMapper.toResponse(product);
    }

    public ProductResponse update(ProductRequest productRequest, UUID id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));

        product.setName(productRequest.productName());
        product.setDescription(productRequest.productDescription());
        product.setQuantity(productRequest.productQuantity());
        product.setStatus(productRequest.productStatus());
        
        // Convert categoryId String to Category entity
        if (productRequest.categoryId() != null) {
            Category category = categoryRepository.findById(UUID.fromString(productRequest.categoryId()))
                    .orElseThrow(() -> new EntityNotFoundException("Category not found"));
            product.setCategory(category);
        }

        productRepository.save(product);
        return productMapper.toResponse(product);
    }

    public String delete(UUID id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));

        productRepository.delete(product);

        return "product deleted successfully";
    }
}
