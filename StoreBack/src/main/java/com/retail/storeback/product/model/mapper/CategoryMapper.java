package com.retail.storeback.product.model.mapper;

import com.retail.storeback.product.dto.CategoryRequestDTO;
import com.retail.storeback.product.dto.CategoryResponseDTO;
import com.retail.storeback.product.model.Category;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface CategoryMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "parent", ignore = true)
    @Mapping(target = "children", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "products", ignore = true)
    Category toEntity(CategoryRequestDTO request);

    @Mapping(target = "parentId", ignore = true)
    CategoryResponseDTO toResponse(Category category);
}
