package com.retail.storeback.category.controller;

import com.retail.storeback.category.model.dto.CategoryRequest;
import com.retail.storeback.category.model.dto.CategoryResponse;
import com.retail.storeback.category.service.CategoryService;
import lombok.AllArgsConstructor;
import org.apache.coyote.Response;
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
    public ResponseEntity<List<CategoryResponse>> getAllCategories() {

        return ResponseEntity.ok(categoryService.getList());
    }

    @GetMapping("/category/{id}")
    public ResponseEntity<CategoryResponse> getCategoryById(@RequestParam UUID id) {

        return ResponseEntity.ok(categoryService.getById(id));
    }

    @PostMapping("/category")
    public ResponseEntity<CategoryResponse> createCategory(@RequestBody CategoryRequest categoryRequest) {

        return ResponseEntity.ok(categoryService.create(categoryRequest));
    }

    @PatchMapping("/category")
    public ResponseEntity<CategoryResponse> updateCategory(@RequestBody CategoryRequest categoryRequest, @RequestParam UUID id) {

        return ResponseEntity.ok(categoryService.update(categoryRequest, id));
    }

    @DeleteMapping("/category")
    public ResponseEntity<String> deleteCategory(@RequestParam UUID id) {

        return ResponseEntity.ok(categoryService.delete(id));
    }
}
