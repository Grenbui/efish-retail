package com.retail.storeback.category.service;

import com.retail.storeback.category.model.Category;
import com.retail.storeback.category.model.dto.CategoryRequest;
import com.retail.storeback.category.model.dto.CategoryResponse;
import com.retail.storeback.category.model.mapper.CategoryMapper;
import com.retail.storeback.category.model.mapper.CategoryMapperImpl;
import com.retail.storeback.category.repository.CategoryRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    private final CategoryMapper categoryMapper = new CategoryMapperImpl();

    private CategoryService categoryService;

    @BeforeEach
    void setUp() {
        categoryService = new CategoryService(categoryRepository, categoryMapper);
    }

    @Test
    @DisplayName("Tạo mới danh mục thành công khi request hợp lệ")
    void create_whenValidRequest_shouldReturnCreatedCategoryResponse() {
        CategoryRequest request = CategoryRequest.builder()
                .categoryName("Cá Betta")
                .categoryDescription("Cá xiêm cảnh thuần chủng")
                .build();

        UUID generatedId = UUID.randomUUID();
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> {
            Category entity = invocation.getArgument(0);
            entity.setId(generatedId);
            return entity;
        });

        CategoryResponse response = categoryService.create(request);

        assertThat(response).isNotNull();
        assertThat(response.categoryId()).isEqualTo(generatedId);
        assertThat(response.categoryName()).isEqualTo("Cá Betta");
        assertThat(response.categoryDescription()).isEqualTo("Cá xiêm cảnh thuần chủng");
        verify(categoryRepository, times(1)).save(any(Category.class));
    }

    @Test
    @DisplayName("Lấy danh mục theo ID hợp lệ trả về CategoryResponse")
    void getById_whenIdExists_shouldReturnCategoryResponse() {
        UUID id = UUID.randomUUID();
        Category category = Category.builder()
                .id(id)
                .name("Cá Tetra")
                .description("Cá thủy sinh bơi đàn")
                .build();

        when(categoryRepository.findById(id)).thenReturn(Optional.of(category));

        CategoryResponse response = categoryService.getById(id);

        assertThat(response).isNotNull();
        assertThat(response.categoryId()).isEqualTo(id);
        assertThat(response.categoryName()).isEqualTo("Cá Tetra");
        assertThat(response.categoryDescription()).isEqualTo("Cá thủy sinh bơi đàn");
    }

    @Test
    @DisplayName("Lấy danh mục theo ID không tồn tại ném EntityNotFoundException")
    void getById_whenIdNotFound_shouldThrowEntityNotFoundException() {
        UUID id = UUID.randomUUID();
        when(categoryRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.getById(id))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining(id.toString());
    }

    @Test
    @DisplayName("Lấy danh sách trả về toàn bộ danh mục hiện có")
    void getList_whenCategoriesExist_shouldReturnList() {
        Category c1 = Category.builder().id(UUID.randomUUID()).name("Cá Betta").build();
        Category c2 = Category.builder().id(UUID.randomUUID()).name("Cá Guppy").build();
        when(categoryRepository.findAll()).thenReturn(List.of(c1, c2));

        List<CategoryResponse> list = categoryService.getList();

        assertThat(list).hasSize(2);
        assertThat(list.get(0).categoryName()).isEqualTo("Cá Betta");
        assertThat(list.get(1).categoryName()).isEqualTo("Cá Guppy");
    }

    @Test
    @DisplayName("Lấy danh sách khi trống trả về rỗng không gây lỗi")
    void getList_whenEmpty_shouldReturnEmptyList() {
        when(categoryRepository.findAll()).thenReturn(Collections.emptyList());

        List<CategoryResponse> list = categoryService.getList();

        assertThat(list).isEmpty();
    }

    @Test
    @DisplayName("Cập nhật danh mục thành công khi ID tồn tại")
    void update_whenCategoryExists_shouldUpdateAndReturnResponse() {
        UUID id = UUID.randomUUID();
        Category existing = Category.builder()
                .id(id)
                .name("Tên cũ")
                .description("Mô tả cũ")
                .build();

        when(categoryRepository.findById(id)).thenReturn(Optional.of(existing));
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CategoryRequest updateRequest = CategoryRequest.builder()
                .categoryName("Tên mới")
                .categoryDescription("Mô tả mới")
                .build();

        CategoryResponse response = categoryService.update(updateRequest, id);

        assertThat(response).isNotNull();
        assertThat(response.categoryName()).isEqualTo("Tên mới");
        assertThat(response.categoryDescription()).isEqualTo("Mô tả mới");
        verify(categoryRepository, times(1)).save(existing);
    }

    @Test
    @DisplayName("Cập nhật danh mục ném EntityNotFoundException khi ID không tồn tại")
    void update_whenCategoryNotFound_shouldThrowEntityNotFoundException() {
        UUID id = UUID.randomUUID();
        when(categoryRepository.findById(id)).thenReturn(Optional.empty());

        CategoryRequest updateRequest = CategoryRequest.builder()
                .categoryName("Tên mới")
                .build();

        assertThatThrownBy(() -> categoryService.update(updateRequest, id))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining(id.toString());
        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    @DisplayName("Xóa danh mục thành công khi ID tồn tại")
    void delete_whenCategoryExists_shouldDeleteSuccessfully() {
        UUID id = UUID.randomUUID();
        Category category = Category.builder().id(id).name("Cá cần xóa").build();
        when(categoryRepository.findById(id)).thenReturn(Optional.of(category));

        String result = categoryService.delete(id);

        assertThat(result).isEqualTo("Category deleted");
        verify(categoryRepository, times(1)).delete(category);
    }

    @Test
    @DisplayName("Xóa danh mục ném EntityNotFoundException khi ID không tồn tại")
    void delete_whenCategoryNotFound_shouldThrowEntityNotFoundException() {
        UUID id = UUID.randomUUID();
        when(categoryRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.delete(id))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Category not found");
        verify(categoryRepository, never()).delete(any(Category.class));
    }
}
