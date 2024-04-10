package com.onlineauction.OnlineAuction.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import com.onlineauction.OnlineAuction.dto.CategoryDTO;
import com.onlineauction.OnlineAuction.entity.Category;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    CategoryMapper INSTANCE = Mappers.getMapper(CategoryMapper.class);

    CategoryDTO categoryToCategoryDto(Category category);

    Category categoryDtoToCategory(CategoryDTO categoryDTO);
}
