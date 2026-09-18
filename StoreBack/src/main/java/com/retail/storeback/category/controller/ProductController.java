package com.retail.storeback.category.controller;

import com.retail.storeback.category.model.dto.ProductRequest;
import com.retail.storeback.category.model.dto.ProductResponse;
import com.retail.storeback.category.service.ProductService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@AllArgsConstructor
public class ProductController {

    public final ProductService productService;

    @GetMapping("/products")
    public ResponseEntity<List<ProductResponse>> getAllProduct() {

        return ResponseEntity.ok(productService.getAllProducts());
    }

    @GetMapping("/product/{id}")
    public ResponseEntity<ProductResponse> getProductById(@RequestParam UUID id) {

        return ResponseEntity.ok(productService.getProductById(id));
    }

    @PostMapping("/product")
    public ResponseEntity<ProductResponse> createProduct(@RequestBody ProductRequest productRequest) {

        return ResponseEntity.ok(productService.create(productRequest));
    }

    @PatchMapping("/product")
    public ResponseEntity<ProductResponse> updateProduct(@RequestParam UUID id, @RequestBody ProductRequest productRequest) {

        return ResponseEntity.ok(productService.update(productRequest, id));
    }

    @DeleteMapping("/product")
    public ResponseEntity<String> deleteProduct(@RequestParam UUID id) {

        return ResponseEntity.ok(productService.delete(id));
    }
}
