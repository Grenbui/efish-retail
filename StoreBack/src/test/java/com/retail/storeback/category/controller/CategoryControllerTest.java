package com.retail.storeback.category.controller;

import com.retail.storeback.category.model.dto.CategoryRequest;
import com.retail.storeback.category.model.dto.CategoryResponse;
import com.retail.storeback.category.service.CategoryService;
import com.retail.storeback.utils.GlobalExceptionHandler;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class CategoryControllerTest {

    private MockMvc mockMvc;

    @Mock
    private CategoryService categoryService;

    @InjectMocks
    private CategoryController categoryController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(categoryController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("GET /api/categories - Trả về 200 và danh sách category")
    void getAllCategories_whenCalled_shouldReturn200AndList() throws Exception {
        UUID id = UUID.randomUUID();
        CategoryResponse response = CategoryResponse.builder()
                .categoryId(id)
                .categoryName("Cá Tetra")
                .categoryDescription("Cá bơi đàn")
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        when(categoryService.getList()).thenReturn(List.of(response));

        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].categoryId").value(id.toString()))
                .andExpect(jsonPath("$[0].categoryName").value("Cá Tetra"))
                .andExpect(jsonPath("$[0].categoryDescription").value("Cá bơi đàn"));
    }

    @Test
    @DisplayName("GET /api/categories - Trả về 200 và danh sách rỗng khi không có danh mục")
    void getAllCategories_whenEmpty_shouldReturn200AndEmptyArray() throws Exception {
        when(categoryService.getList()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @DisplayName("GET /api/category/{id} - Trả về 200 khi ID tồn tại")
    void getCategoryById_whenExists_shouldReturn200AndCategory() throws Exception {
        UUID id = UUID.randomUUID();
        CategoryResponse response = CategoryResponse.builder()
                .categoryId(id)
                .categoryName("Cá Betta")
                .categoryDescription("Cá cảnh chọi")
                .build();

        when(categoryService.getById(id)).thenReturn(response);

        mockMvc.perform(get("/api/category/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categoryId").value(id.toString()))
                .andExpect(jsonPath("$.categoryName").value("Cá Betta"));
    }

    @Test
    @DisplayName("GET /api/category/{id} - Trả về 404 khi ID không tồn tại")
    void getCategoryById_whenNotFound_shouldReturn404() throws Exception {
        UUID id = UUID.randomUUID();
        when(categoryService.getById(id)).thenThrow(new EntityNotFoundException("Category not found with id: " + id));

        mockMvc.perform(get("/api/category/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Category not found with id: " + id));
    }

    @Test
    @DisplayName("POST /api/category - Trả về 201 Created khi tạo mới thành công")
    void createCategory_whenValid_shouldReturn201Created() throws Exception {
        UUID id = UUID.randomUUID();
        CategoryResponse response = CategoryResponse.builder()
                .categoryId(id)
                .categoryName("Cá Thủy Sinh")
                .categoryDescription("Các dòng cá nuôi hồ thủy sinh")
                .build();

        when(categoryService.create(any(CategoryRequest.class))).thenReturn(response);

        String jsonPayload = """
                {
                    "categoryName": "Cá Thủy Sinh",
                    "categoryDescription": "Các dòng cá nuôi hồ thủy sinh"
                }
                """;

        mockMvc.perform(post("/api/category")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.categoryId").value(id.toString()))
                .andExpect(jsonPath("$.categoryName").value("Cá Thủy Sinh"));
    }

    @Test
    @DisplayName("PATCH /api/category/{id} - Trả về 200 khi cập nhật thành công")
    void updateCategory_whenValid_shouldReturn200() throws Exception {
        UUID id = UUID.randomUUID();
        CategoryResponse response = CategoryResponse.builder()
                .categoryId(id)
                .categoryName("Tên đã sửa")
                .categoryDescription("Mô tả đã sửa")
                .build();

        when(categoryService.update(any(CategoryRequest.class), eq(id))).thenReturn(response);

        String jsonPayload = """
                {
                    "categoryName": "Tên đã sửa",
                    "categoryDescription": "Mô tả đã sửa"
                }
                """;

        mockMvc.perform(patch("/api/category/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categoryId").value(id.toString()))
                .andExpect(jsonPath("$.categoryName").value("Tên đã sửa"));
    }

    @Test
    @DisplayName("PATCH /api/category/{id} - Trả về 404 khi ID không tồn tại")
    void updateCategory_whenNotFound_shouldReturn404() throws Exception {
        UUID id = UUID.randomUUID();
        when(categoryService.update(any(CategoryRequest.class), eq(id)))
                .thenThrow(new EntityNotFoundException("Category not found with id: " + id));

        String jsonPayload = """
                {
                    "categoryName": "Tên đã sửa"
                }
                """;

        mockMvc.perform(patch("/api/category/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Category not found with id: " + id));
    }

    @Test
    @DisplayName("DELETE /api/category/{id} - Trả về 200 khi xóa thành công")
    void deleteCategory_whenExists_shouldReturn200() throws Exception {
        UUID id = UUID.randomUUID();
        when(categoryService.delete(id)).thenReturn("Category deleted successfully");

        mockMvc.perform(delete("/api/category/{id}", id))
                .andExpect(status().isOk())
                .andExpect(content().string("Category deleted successfully"));
    }

    @Test
    @DisplayName("DELETE /api/category/{id} - Trả về 404 khi ID không tồn tại")
    void deleteCategory_whenNotFound_shouldReturn404() throws Exception {
        UUID id = UUID.randomUUID();
        when(categoryService.delete(id)).thenThrow(new EntityNotFoundException("Category not found with id: " + id));

        mockMvc.perform(delete("/api/category/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Category not found with id: " + id));
    }
}
