package com.retail.storeback.category.service;

import com.retail.storeback.category.model.Category;
import com.retail.storeback.category.model.dto.CategoryRequest;
import com.retail.storeback.category.model.dto.CategoryResponse;
import com.retail.storeback.category.model.mapper.CategoryMapper;
import com.retail.storeback.category.repository.CategoryRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Transactional(readOnly = true)
    public List<CategoryResponse> getList() {
        return categoryRepository.findAll().stream()
                .map(categoryMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public CategoryResponse getById(UUID id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Category not found with id: " + id));
        return categoryMapper.toResponse(category);
    }

    @Transactional
    public CategoryResponse create(CategoryRequest categoryRequest) {
        Category category = categoryMapper.toEntity(categoryRequest);
        Category savedCategory = categoryRepository.save(category);
        return categoryMapper.toResponse(savedCategory);
    }

    @Transactional
    public CategoryResponse update(CategoryRequest categoryRequest, UUID id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Category not found with id: " + id));

        categoryMapper.updateEntityFromRequest(categoryRequest, category);

        Category updatedCategory = categoryRepository.save(category);
        return categoryMapper.toResponse(updatedCategory);
    }

    @Transactional
    public String delete(UUID id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Category not found with id: " + id));

        categoryRepository.delete(category);

        return "Category deleted";
    }
}
