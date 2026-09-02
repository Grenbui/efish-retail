package com.retail.storeback.product.service;

import com.retail.storeback.product.dto.CategoryRequestDTO;
import com.retail.storeback.product.dto.CategoryResponseDTO;
import com.retail.storeback.product.model.Category;
import com.retail.storeback.product.model.mapper.CategoryMapper;
import com.retail.storeback.product.repository.CategoryRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    public CategoryResponseDTO create(CategoryRequestDTO request) {
        Category category = categoryMapper.toEntity(request);

        categoryRepository.save(category);

        return categoryMapper.toResponse(category);
    }

    public CategoryResponseDTO update(UUID categoryId, CategoryRequestDTO request) {
        Category category = categoryRepository.findById(categoryId).orElseThrow(() -> new EntityNotFoundException("Category not found"));
        categoryRepository.save(category);

        return categoryMapper.toResponse(category);
    }

    public List<CategoryResponseDTO> listAll() {
        List<Category> categories = categoryRepository.findAll();

        return categories.stream().map(categoryMapper::toResponse).toList();
    }

    public CategoryResponseDTO detail(UUID categoryId) {
        Category category = categoryRepository.findById(categoryId).orElseThrow(() -> new EntityNotFoundException("Category not found"));

        return categoryMapper.toResponse(category);
    }

    public UUID delete(UUID categoryId) {
        Category category = categoryRepository.findById(categoryId).orElseThrow(() -> new EntityNotFoundException("Category not found"));
        categoryRepository.delete(category);

        return category.getId();
    }
}
