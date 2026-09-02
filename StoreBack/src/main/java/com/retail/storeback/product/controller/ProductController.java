package com.retail.storeback.product.controller;

import com.retail.storeback.product.dto.*;
import com.retail.storeback.product.service.ProductService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@AllArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping("/product")
    public ResponseEntity<ProductResponseDTO> createProduct(@RequestBody @Valid ProductRequestDTO request) {

        return ResponseEntity.ok(productService.create(request));
    }

    @GetMapping("/products")
    public ResponseEntity<List<ProductResponseDTO>> getAllProducts() {

        return ResponseEntity.ok(productService.listAll());
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<ProductDetailResponseDTO> getProductDetail(@PathVariable("productId") UUID productId) {

        return ResponseEntity.ok(productService.detail(productId));
    }

    @PatchMapping("/product/{productId}")
    public ResponseEntity<ProductDetailResponseDTO> updateProduct(@RequestBody @Valid ProductRequestDTO request, @PathVariable UUID productId) {

        return ResponseEntity.ok(productService.update(productId, request));
    }

    @DeleteMapping("/product")
    public ResponseEntity<?> deleteProduct(@RequestParam UUID productId) {

        return  ResponseEntity.ok(productService.delete(productId));
    }
}
