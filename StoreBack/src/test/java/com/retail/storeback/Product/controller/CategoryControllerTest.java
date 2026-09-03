package com.retail.storeback.Product.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.retail.storeback.product.controller.CategoryController;
import com.retail.storeback.product.dto.CategoryRequestDTO;
import com.retail.storeback.product.dto.CategoryResponseDTO;
import com.retail.storeback.product.service.CategoryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@WebMvcTest(CategoryController.class)
@ActiveProfiles("dev")
public class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private CategoryService categoryService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void createCategory_withoutParent_shouldReturn200() throws Exception {

        // Input
        CategoryRequestDTO request = CategoryRequestDTO.builder()
                .name("fish")
                .slug("tetra")
                .parentId(null)
                .build();

        // Expected Output
        CategoryResponseDTO response = CategoryResponseDTO.builder()
                .id(UUID.randomUUID())
                .name("fish")
                .slug("tetra")
                .parentId(null)
                .build();

        String jsonRequest = objectMapper.writeValueAsString(request);

        when(categoryService.create(request)).thenReturn(response);

        mockMvc.perform(post("/api/category")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(response.id().toString()))
                .andExpect(jsonPath("$.name").value("fish"))
                .andExpect(jsonPath("$.slug").value("tetra"))
                .andExpect(jsonPath("$.parentId").doesNotExist());
    }
}
