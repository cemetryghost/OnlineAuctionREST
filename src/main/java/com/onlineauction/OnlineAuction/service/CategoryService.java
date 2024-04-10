package com.onlineauction.OnlineAuction.service;

import com.onlineauction.OnlineAuction.dto.CategoryDTO;

import java.util.List;

public interface CategoryService {
    CategoryDTO getCategoryById(Long id);
    List<CategoryDTO> getAllCategories();
    CategoryDTO createCategory(CategoryDTO categoryDTO);
    void deleteCategory(Long id);
    boolean updateCategoryName(Long id, String newName);
}
