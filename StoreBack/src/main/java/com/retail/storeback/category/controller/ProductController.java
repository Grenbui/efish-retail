package com.retail.storeback.category.controller;

import com.retail.storeback.category.model.dto.ProductRequest;
import com.retail.storeback.category.model.dto.ProductResponse;
import com.retail.storeback.category.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping("/products")
    public ResponseEntity<List<ProductResponse>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    @GetMapping("/product/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable UUID id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @GetMapping("/products/category/{categoryId}")
    public ResponseEntity<List<ProductResponse>> getProductsByCategory(@PathVariable UUID categoryId) {
        return ResponseEntity.ok(productService.getProductsByCategory(categoryId));
    }

    @PostMapping("/product")
    public ResponseEntity<ProductResponse> createProduct(@RequestBody ProductRequest productRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.create(productRequest));
    }

    @PatchMapping({"/product/{id}", "/product"})
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable(required = false) UUID id,
            @RequestParam(required = false) UUID productId,
            @RequestBody ProductRequest productRequest) {
        UUID targetId = id != null ? id : productId;
        return ResponseEntity.ok(productService.update(productRequest, targetId));
    }

    @DeleteMapping({"/product/{id}", "/product"})
    public ResponseEntity<String> deleteProduct(
            @PathVariable(required = false) UUID id,
            @RequestParam(required = false) UUID productId) {
        UUID targetId = id != null ? id : productId;
        return ResponseEntity.ok(productService.delete(targetId));
    }
}
