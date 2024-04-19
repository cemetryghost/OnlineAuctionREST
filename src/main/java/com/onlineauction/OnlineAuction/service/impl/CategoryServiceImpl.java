package com.onlineauction.OnlineAuction.service.impl;

import com.onlineauction.OnlineAuction.dto.CategoryDTO;
import com.onlineauction.OnlineAuction.entity.Category;
import com.onlineauction.OnlineAuction.mapper.CategoryMapper;
import com.onlineauction.OnlineAuction.repository.CategoryRepository;
import com.onlineauction.OnlineAuction.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Validated
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Autowired
    public CategoryServiceImpl(CategoryRepository categoryRepository, CategoryMapper categoryMapper) {
        this.categoryRepository = categoryRepository;
        this.categoryMapper = categoryMapper;
    }

    @Override
    public CategoryDTO getCategoryById(Long id) {
        Category category = categoryRepository.findById(id).orElse(null);
        return category != null ? categoryMapper.categoryToCategoryDto(category) : null;
    }

    @Override
    public List<CategoryDTO> getAllCategories() {
        List<Category> categories = categoryRepository.findAll();
        return categories.stream().map(categoryMapper::categoryToCategoryDto).collect(Collectors.toList());
    }

    @Override
    public CategoryDTO createCategory(CategoryDTO categoryDTO) {
        if(categoryRepository.existsByNameCategory(categoryDTO.getNameCategory())) {
            throw new DataIntegrityViolationException("Категория с именем " + categoryDTO.getNameCategory() + " уже существует");
        }
        Category category = categoryMapper.categoryDtoToCategory(categoryDTO);
        category = categoryRepository.save(category);
        return categoryMapper.categoryToCategoryDto(category);
    }

    @Override
    public void deleteCategory(Long id) {
        categoryRepository.deleteById(id);
    }

    @Override
    public boolean updateCategoryName(Long id, String newName) {
        return categoryRepository.findById(id).map(category -> {
            if(categoryRepository.existsByNameCategory(newName)) {
                throw new DataIntegrityViolationException("Категория с именем " + newName + " уже существует");
            }
            category.setNameCategory(newName);
            categoryRepository.save(category);
            return true;
        }).orElse(false);
    }
}
