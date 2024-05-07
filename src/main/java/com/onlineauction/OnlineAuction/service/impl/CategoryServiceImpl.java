package com.onlineauction.OnlineAuction.service.impl;

import com.onlineauction.OnlineAuction.dto.CategoryDTO;
import com.onlineauction.OnlineAuction.entity.Category;
import com.onlineauction.OnlineAuction.exception.CategoryException;
import com.onlineauction.OnlineAuction.mapper.CategoryMapper;
import com.onlineauction.OnlineAuction.repository.CategoryRepository;
import com.onlineauction.OnlineAuction.service.CategoryService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    public CategoryServiceImpl(CategoryRepository categoryRepository, CategoryMapper categoryMapper) {
        this.categoryRepository = categoryRepository;
        this.categoryMapper = categoryMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryDTO getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .map(categoryMapper::categoryToCategoryDto)
                .orElseThrow(() -> new DataIntegrityViolationException("Category not found with ID: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryDTO> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(categoryMapper::categoryToCategoryDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CategoryDTO createCategory(CategoryDTO categoryDTO) {
        if (!StringUtils.hasText(categoryDTO.getNameCategory())) {
            throw new CategoryException("Имя категории не может быть пустым");
        }
        if (categoryRepository.existsByNameCategory(categoryDTO.getNameCategory())) {
            throw new CategoryException("Категория с именем \"" + categoryDTO.getNameCategory() + "\" уже существует");
        }
        Category category = categoryMapper.categoryDtoToCategory(categoryDTO);
        category = categoryRepository.save(category);
        return categoryMapper.categoryToCategoryDto(category);
    }

    @Override
    @Transactional
    public void deleteCategory(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new CategoryException("Категория с ID " + id + " не существует");
        }
        try {
            categoryRepository.deleteById(id);
        } catch (DataIntegrityViolationException e) {
            throw new CategoryException("Cannot delete category with ID " + id + " because it is associated with other entities (lots)");
        }
    }

    @Override
    @Transactional
    public boolean updateCategoryName(Long id, String newName) {
        if (!StringUtils.hasText(newName)) {
            throw new CategoryException("Имя категории не может быть пустым");
        }
        return categoryRepository.findById(id).map(category -> {
            if (categoryRepository.existsByNameCategory(newName)) {
                throw new CategoryException("Категория с именем \"" + newName + "\" уже существует");
            }
            category.setNameCategory(newName);
            categoryRepository.save(category);
            return true;
        }).orElseThrow(() -> new CategoryException("Category not found with ID: " + id));
    }
}
