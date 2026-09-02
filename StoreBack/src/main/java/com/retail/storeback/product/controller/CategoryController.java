package com.retail.storeback.product.controller;

import com.retail.storeback.product.dto.CategoryRequestDTO;
import com.retail.storeback.product.dto.CategoryResponseDTO;
import com.retail.storeback.product.service.CategoryService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@AllArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping("/categories")
    public ResponseEntity<List<CategoryResponseDTO>> listAll() {

        return ResponseEntity.ok(categoryService.listAll());
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<CategoryResponseDTO> getDetail(@PathVariable UUID categoryId) {

        return ResponseEntity.ok(categoryService.detail(categoryId));
    }

    @PostMapping("/category")
    public ResponseEntity<CategoryResponseDTO> create(@Valid @RequestBody CategoryRequestDTO request) {

        return ResponseEntity.ok(categoryService.create(request));
    }

    @PatchMapping("/category/{categoryId}")
    public ResponseEntity<CategoryResponseDTO> update(@PathVariable UUID categoryId, @Valid @RequestBody CategoryRequestDTO request) {

        return ResponseEntity.ok(categoryService.update(categoryId, request));
    }

    @DeleteMapping("/category")
    public ResponseEntity<?> delete(@PathVariable UUID categoryId) {

        return ResponseEntity.ok(categoryService.delete(categoryId));
    }
}
