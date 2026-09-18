package com.retail.storeback.category.model.mapper;

import com.retail.storeback.category.model.Category;
import com.retail.storeback.category.model.dto.CategoryRequest;
import com.retail.storeback.category.model.dto.CategoryResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(source = "categoryName", target = "name")
    @Mapping(source = "categoryDescription", target = "description")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Category toEntity(CategoryRequest request);

    @Mapping(source = "id", target = "categoryId")
    @Mapping(source = "name", target = "categoryName")
    @Mapping(source = "description", target = "categoryDescription")
    CategoryResponse toResponse(Category entity);
}