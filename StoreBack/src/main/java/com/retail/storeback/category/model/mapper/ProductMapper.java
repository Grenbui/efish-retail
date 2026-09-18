package com.retail.storeback.category.model.mapper;

import com.retail.storeback.category.model.Category;
import com.retail.storeback.category.model.Product;
import com.retail.storeback.category.model.dto.ProductRequest;
import com.retail.storeback.category.model.dto.ProductResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.UUID;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(source = "categoryId", target = "category")
    @Mapping(source = "productName", target = "name")
    @Mapping(source = "productDescription", target = "description")
    @Mapping(source = "productQuantity", target = "quantity")
    @Mapping(source = "productStatus", target = "status")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Product toEntity(ProductRequest productRequest);

    @Mapping(source = "id", target = "productId")
    @Mapping(source = "category.id", target = "categoryId")
    @Mapping(source = "name", target = "productName")
    @Mapping(source = "description", target = "productDescription")
    @Mapping(source = "quantity", target = "productQuantity")
    @Mapping(source = "status", target = "productStatus")
    ProductResponse toResponse(Product product);

    // Custom mapping: Convert String categoryId to Category entity
    default Category map(String categoryId) {
        if (categoryId == null) {
            return null;
        }
        Category category = new Category();
        category.setId(UUID.fromString(categoryId));
        return category;
    }

    // Custom mapping: Convert UUID to String
    default String map(UUID id) {
        return id != null ? id.toString() : null;
    }
}
