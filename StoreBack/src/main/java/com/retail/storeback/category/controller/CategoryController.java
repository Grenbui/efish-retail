package com.retail.storeback.category.controller;

import com.retail.storeback.category.model.dto.CategoryRequest;
import com.retail.storeback.category.model.dto.CategoryResponse;
import com.retail.storeback.category.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping("/categories")
    public ResponseEntity<List<CategoryResponse>> getAllCategories() {
        return ResponseEntity.ok(categoryService.getList());
    }

    @GetMapping("/category/{id}")
    public ResponseEntity<CategoryResponse> getCategoryById(@PathVariable UUID id) {
        return ResponseEntity.ok(categoryService.getById(id));
    }

    @PostMapping("/category")
    public ResponseEntity<CategoryResponse> createCategory(@RequestBody CategoryRequest categoryRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(categoryService.create(categoryRequest));
    }

    @PatchMapping({"/category/{id}", "/category"})
    public ResponseEntity<CategoryResponse> updateCategory(
            @PathVariable(required = false) UUID id,
            @RequestParam(required = false) UUID categoryId,
            @RequestBody CategoryRequest categoryRequest) {
        UUID targetId = id != null ? id : categoryId;
        return ResponseEntity.ok(categoryService.update(categoryRequest, targetId));
    }

    @DeleteMapping({"/category/{id}", "/category"})
    public ResponseEntity<String> deleteCategory(
            @PathVariable(required = false) UUID id,
            @RequestParam(required = false) UUID categoryId) {
        UUID targetId = id != null ? id : categoryId;
        return ResponseEntity.ok(categoryService.delete(targetId));
    }
}
