package com.retail.storeback.product.service;

import com.retail.storeback.product.dto.*;
import com.retail.storeback.product.model.Product;
import com.retail.storeback.product.model.mapper.ProductMapper;
import com.retail.storeback.product.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    public ProductResponseDTO create(ProductRequestDTO request) {
        Product product = productMapper.toEntity(request);
        productRepository.save(product);

        return productMapper.toResponse(product);
    }

    public List<ProductResponseDTO> listAll() {
        List<Product> products = productRepository.findAll();

        return products.stream().map(productMapper::toResponse).toList();
    }

    public ProductDetailResponseDTO detail(UUID productId) {
        Optional<Product> product = productRepository.findById(productId);
        if (product.isEmpty()) {
            throw new EntityNotFoundException("Product not found");
        }

        return productMapper.toDetailResponse(product.get());
    }

    public ProductDetailResponseDTO update(UUID productId, ProductRequestDTO request) {

        Product product = productRepository.findById(productId).orElseThrow(() -> new EntityNotFoundException("Product not found"));
        productRepository.save(product);
        return productMapper.toDetailResponse(product);
    }

    public UUID delete(UUID productId) {
        Optional<Product> product = productRepository.findById(productId);
        if (product.isPresent()) {
            productRepository.delete(product.get());
        } else {
            throw new EntityNotFoundException("Product not found");
        }

        return productId;
    }
}
