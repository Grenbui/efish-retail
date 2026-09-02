package com.retail.storeback.product.model.mapper;

import com.retail.storeback.product.dto.*;
import com.retail.storeback.product.model.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface ProductMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Product toEntity (ProductRequestDTO request);

    @Mapping(target = "categoryId", source = "category.id")
    ProductResponseDTO toResponse(Product product);

    default ProductDetailResponseDTO toDetailResponse(Product product) {
        if (product == null) {
            return null;
        }
        return new ProductDetailResponseDTO(
            toResponse(product),
            product.getDescription()
        );
    }
}
