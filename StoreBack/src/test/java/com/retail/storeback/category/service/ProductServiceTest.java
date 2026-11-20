package com.retail.storeback.category.service;

import com.retail.storeback.category.model.Category;
import com.retail.storeback.category.model.Product;
import com.retail.storeback.category.model.dto.ProductRequest;
import com.retail.storeback.category.model.dto.ProductResponse;
import com.retail.storeback.category.model.mapper.ProductMapper;
import com.retail.storeback.category.model.mapper.ProductMapperImpl;
import com.retail.storeback.category.repository.CategoryRepository;
import com.retail.storeback.category.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    private final ProductMapper productMapper = new ProductMapperImpl();

    private ProductService productService;

    private Category sampleCategory;
    private UUID categoryId;

    @BeforeEach
    void setUp() {
        productService = new ProductService(productRepository, categoryRepository, productMapper);

        categoryId = UUID.randomUUID();
        sampleCategory = Category.builder()
                .id(categoryId)
                .name("Cá Tetra")
                .description("Cá thủy sinh")
                .build();
    }

    @Test
    @DisplayName("Tạo sản phẩm cá cảnh thành công với đầy đủ thông tin")
    void create_whenValidProduct_shouldCreateAndReturnProductResponse() {
        ProductRequest request = ProductRequest.builder()
                .categoryId(categoryId)
                .sku("FISH-TETRA-NEON-M")
                .productName("Cá Neon Vua")
                .productDescription("Màu sắc óng ánh, khỏe mạnh")
                .sizeLabel("Size M 3-4cm")
                .sellingPrice(new BigDecimal("25000.00"))
                .imageUrl("https://example.com/neon.jpg")
                .active(true)
                .build();

        when(productRepository.existsBySku("FISH-TETRA-NEON-M")).thenReturn(false);
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(sampleCategory));

        UUID generatedId = UUID.randomUUID();
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> {
            Product p = invocation.getArgument(0);
            p.setId(generatedId);
            return p;
        });

        ProductResponse response = productService.create(request);

        assertThat(response).isNotNull();
        assertThat(response.productId()).isEqualTo(generatedId);
        assertThat(response.categoryId()).isEqualTo(categoryId);
        assertThat(response.categoryName()).isEqualTo("Cá Tetra");
        assertThat(response.sku()).isEqualTo("FISH-TETRA-NEON-M");
        assertThat(response.productName()).isEqualTo("Cá Neon Vua");
        assertThat(response.sizeLabel()).isEqualTo("Size M 3-4cm");
        assertThat(response.sellingPrice()).isEqualByComparingTo(new BigDecimal("25000.00"));
        assertThat(response.active()).isTrue();
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    @DisplayName("Tạo sản phẩm ném IllegalArgumentException khi trùng SKU")
    void create_whenSkuAlreadyExists_shouldThrowIllegalArgumentException() {
        ProductRequest request = ProductRequest.builder()
                .categoryId(categoryId)
                .sku("FISH-DUPLICATE-SKU")
                .productName("Cá Bảy Màu Dumbo")
                .build();

        when(productRepository.existsBySku("FISH-DUPLICATE-SKU")).thenReturn(true);

        assertThatThrownBy(() -> productService.create(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("SKU already exists: FISH-DUPLICATE-SKU");

        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("Tạo sản phẩm ném EntityNotFoundException khi Category không tồn tại")
    void create_whenCategoryNotFound_shouldThrowEntityNotFoundException() {
        ProductRequest request = ProductRequest.builder()
                .categoryId(categoryId)
                .sku("FISH-BETTA-HALFMOON")
                .productName("Cá Betta Halfmoon")
                .build();

        when(productRepository.existsBySku("FISH-BETTA-HALFMOON")).thenReturn(false);
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.create(request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Category not found with id: " + categoryId);

        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("Lấy sản phẩm theo ID hợp lệ trả về ProductResponse")
    void getProductById_whenIdExists_shouldReturnProductResponse() {
        UUID productId = UUID.randomUUID();
        Product product = Product.builder()
                .id(productId)
                .category(sampleCategory)
                .sku("FISH-BETTA-001")
                .name("Betta Fancy Cooper")
                .sizeLabel("Size L (5-6cm)")
                .sellingPrice(new BigDecimal("150000.00"))
                .active(true)
                .build();

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        ProductResponse response = productService.getProductById(productId);

        assertThat(response).isNotNull();
        assertThat(response.productId()).isEqualTo(productId);
        assertThat(response.categoryName()).isEqualTo("Cá Tetra");
        assertThat(response.sku()).isEqualTo("FISH-BETTA-001");
        assertThat(response.productName()).isEqualTo("Betta Fancy Cooper");
        assertThat(response.sizeLabel()).isEqualTo("Size L (5-6cm)");
        assertThat(response.sellingPrice()).isEqualByComparingTo(new BigDecimal("150000.00"));
    }

    @Test
    @DisplayName("Lấy sản phẩm theo ID không tồn tại ném EntityNotFoundException")
    void getProductById_whenIdNotFound_shouldThrowEntityNotFoundException() {
        UUID productId = UUID.randomUUID();
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getProductById(productId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Product not found with id: " + productId);
    }

    @Test
    @DisplayName("Lấy toàn bộ danh sách sản phẩm trả về danh sách đầy đủ")
    void getAllProducts_whenCalled_shouldReturnList() {
        Product p1 = Product.builder().id(UUID.randomUUID()).category(sampleCategory).name("Cá 1").sku("SKU1").sellingPrice(BigDecimal.TEN).build();
        Product p2 = Product.builder().id(UUID.randomUUID()).category(sampleCategory).name("Cá 2").sku("SKU2").sellingPrice(BigDecimal.ONE).build();
        when(productRepository.findAll()).thenReturn(List.of(p1, p2));

        List<ProductResponse> list = productService.getAllProducts();

        assertThat(list).hasSize(2);
    }

    @Test
    @DisplayName("Lọc sản phẩm theo CategoryId trả về danh sách sản phẩm của danh mục")
    void getProductsByCategory_whenHasProducts_shouldReturnList() {
        Product p = Product.builder().id(UUID.randomUUID()).category(sampleCategory).name("Cá Guppy Rồng Đỏ").sku("GUP-RED").sellingPrice(BigDecimal.TEN).build();
        when(productRepository.findByCategoryId(categoryId)).thenReturn(List.of(p));

        List<ProductResponse> list = productService.getProductsByCategory(categoryId);

        assertThat(list).hasSize(1);
        assertThat(list.get(0).sku()).isEqualTo("GUP-RED");
    }

    @Test
    @DisplayName("Lọc sản phẩm theo CategoryId trả về danh sách rỗng khi chưa có sản phẩm")
    void getProductsByCategory_whenNoProducts_shouldReturnEmptyList() {
        when(productRepository.findByCategoryId(categoryId)).thenReturn(Collections.emptyList());

        List<ProductResponse> list = productService.getProductsByCategory(categoryId);

        assertThat(list).isEmpty();
    }

    @Test
    @DisplayName("Cập nhật giá bán, sizeLabel, tên sản phẩm thành công")
    void update_whenValid_shouldUpdatePriceSizeNameAndReturnResponse() {
        UUID productId = UUID.randomUUID();
        Product existing = Product.builder()
                .id(productId)
                .category(sampleCategory)
                .sku("FISH-001")
                .name("Cá Cũ")
                .sizeLabel("Size S")
                .sellingPrice(new BigDecimal("10000.00"))
                .active(true)
                .build();

        when(productRepository.findById(productId)).thenReturn(Optional.of(existing));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProductRequest updateRequest = ProductRequest.builder()
                .productName("Cá Mới Đã Cập Nhật")
                .sizeLabel("Size XL (7-8cm)")
                .sellingPrice(new BigDecimal("250000.00"))
                .build();

        ProductResponse response = productService.update(updateRequest, productId);

        assertThat(response).isNotNull();
        assertThat(response.productName()).isEqualTo("Cá Mới Đã Cập Nhật");
        assertThat(response.sizeLabel()).isEqualTo("Size XL (7-8cm)");
        assertThat(response.sellingPrice()).isEqualByComparingTo(new BigDecimal("250000.00"));
        assertThat(response.sku()).isEqualTo("FISH-001");
    }

    @Test
    @DisplayName("Cập nhật khi giữ nguyên SKU của chính nó thì cho phép thành công")
    void update_whenSkuUnchanged_shouldAllowAndSucceed() {
        UUID productId = UUID.randomUUID();
        Product existing = Product.builder()
                .id(productId)
                .category(sampleCategory)
                .sku("FISH-SAME-SKU")
                .name("Cá giữ nguyên SKU")
                .sellingPrice(new BigDecimal("50000.00"))
                .build();

        when(productRepository.findById(productId)).thenReturn(Optional.of(existing));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProductRequest updateRequest = ProductRequest.builder()
                .sku("FISH-SAME-SKU")
                .productName("Đổi tên nhẹ")
                .build();

        ProductResponse response = productService.update(updateRequest, productId);

        assertThat(response.sku()).isEqualTo("FISH-SAME-SKU");
        assertThat(response.productName()).isEqualTo("Đổi tên nhẹ");
        verify(productRepository, never()).existsBySku("FISH-SAME-SKU");
    }

    @Test
    @DisplayName("Cập nhật SKU trùng với sản phẩm khác ném IllegalArgumentException")
    void update_whenSkuChangedToExistingSku_shouldThrowIllegalArgumentException() {
        UUID productId = UUID.randomUUID();
        Product existing = Product.builder()
                .id(productId)
                .category(sampleCategory)
                .sku("ORIGINAL-SKU")
                .name("Cá A")
                .sellingPrice(BigDecimal.TEN)
                .build();

        when(productRepository.findById(productId)).thenReturn(Optional.of(existing));
        when(productRepository.existsBySku("ANOTHER-EXISTING-SKU")).thenReturn(true);

        ProductRequest updateRequest = ProductRequest.builder()
                .sku("ANOTHER-EXISTING-SKU")
                .build();

        assertThatThrownBy(() -> productService.update(updateRequest, productId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("SKU already exists: ANOTHER-EXISTING-SKU");

        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("Chuyển sản phẩm sang danh mục khác hợp lệ thành công")
    void update_whenCategorySwitchedToValidCategory_shouldSucceed() {
        UUID productId = UUID.randomUUID();
        Product existing = Product.builder()
                .id(productId)
                .category(sampleCategory)
                .sku("FISH-SWITCH")
                .name("Cá Đổi Hệ")
                .sellingPrice(BigDecimal.TEN)
                .build();

        UUID newCategoryId = UUID.randomUUID();
        Category newCategory = Category.builder().id(newCategoryId).name("Cá Thủy Sinh VIP").build();

        when(productRepository.findById(productId)).thenReturn(Optional.of(existing));
        when(categoryRepository.findById(newCategoryId)).thenReturn(Optional.of(newCategory));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProductRequest updateRequest = ProductRequest.builder()
                .categoryId(newCategoryId)
                .build();

        ProductResponse response = productService.update(updateRequest, productId);

        assertThat(response.categoryId()).isEqualTo(newCategoryId);
        assertThat(response.categoryName()).isEqualTo("Cá Thủy Sinh VIP");
    }

    @Test
    @DisplayName("Chuyển sản phẩm sang danh mục không tồn tại ném EntityNotFoundException")
    void update_whenCategorySwitchedToInvalidCategory_shouldThrowEntityNotFoundException() {
        UUID productId = UUID.randomUUID();
        Product existing = Product.builder()
                .id(productId)
                .category(sampleCategory)
                .sku("FISH-SWITCH-FAIL")
                .name("Cá")
                .sellingPrice(BigDecimal.TEN)
                .build();

        UUID ghostCategoryId = UUID.randomUUID();
        when(productRepository.findById(productId)).thenReturn(Optional.of(existing));
        when(categoryRepository.findById(ghostCategoryId)).thenReturn(Optional.empty());

        ProductRequest updateRequest = ProductRequest.builder()
                .categoryId(ghostCategoryId)
                .build();

        assertThatThrownBy(() -> productService.update(updateRequest, productId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Category not found with id: " + ghostCategoryId);

        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("Xóa sản phẩm thành công khi ID tồn tại")
    void delete_whenIdExists_shouldDeleteSuccessfully() {
        UUID productId = UUID.randomUUID();
        Product product = Product.builder().id(productId).name("Cá Dĩa").build();
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        String result = productService.delete(productId);

        assertThat(result).isEqualTo("Product deleted successfully");
        verify(productRepository, times(1)).delete(product);
    }

    @Test
    @DisplayName("Xóa sản phẩm ném EntityNotFoundException khi ID không tồn tại")
    void delete_whenIdNotFound_shouldThrowEntityNotFoundException() {
        UUID productId = UUID.randomUUID();
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.delete(productId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Product not found with id: " + productId);

        verify(productRepository, never()).delete(any(Product.class));
    }
}
