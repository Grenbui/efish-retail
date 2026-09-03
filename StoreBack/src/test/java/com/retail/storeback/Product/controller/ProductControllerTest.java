package com.retail.storeback.Product.controller;

import com.retail.storeback.product.controller.ProductController;
import com.retail.storeback.product.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(ProductController.class)
@ActiveProfiles("dev")
public class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;

//    @Test
//    @WithMockUser(roles = "ADMIN")
//    void getProduct_shouldReturnParentAndChildren() throws Exception {
//
//        UUID categoryId = UUID.randomUUID();
//        UUID parentId = UUID.randomUUID();
//        UUID childId = UUID.randomUUID();
//
//        Category parentCategory = Category.builder().id(parentId).name("fish").build();
//        Category childCategory = Category.builder().id(childId).name("tetra").build();
//        Category category = Category.builder()
//                .id(categoryId)
//                .name("Neon tetra")
//                .slug("")
//                .parent(parentCategory)
//                .children(List.of(childCategory))
//                .build();
//
//        when(productService.detail(categoryId)).thenReturn(category);
//
//        mockMvc.perform(get("/api/category/{categoryId}", categoryId))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.name").value("Neon tetra"))
//                .andExpect(jsonPath("$.parent.name").value("fish"))
//                .andExpect(jsonPath("$.children[0].name").value("tetra"))
//                .andExpect(jsonPath("$.children.parrent").doesNotExist());
//
//    }
//
//    @Test
//    @WithMockUser(roles = "ADMIN")
//    void getCategory_shouldReturnNullParent_whenRootCategory() throws Exception {
//        Category root = Category.builder()
//                .id(UUID.randomUUID())
//                .name("fish")
//                .parent(null)
//                .children(List.of())
//                .build();
//
//        when(productService.delete(root.getId())).thenReturn(root.getId());
//
//        mockMvc.perform(get("/api/category/{categoryId}", root.getId()))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.name").doesNotExist())
//                .andExpect(jsonPath("$.children").isEmpty());
//    }
}
