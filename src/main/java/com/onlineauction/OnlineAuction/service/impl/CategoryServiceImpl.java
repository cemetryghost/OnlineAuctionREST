package com.onlineauction.OnlineAuction.service.impl;

import com.onlineauction.OnlineAuction.dto.CategoryDTO;
import com.onlineauction.OnlineAuction.entity.Category;
import com.onlineauction.OnlineAuction.exception.CategoryException;
import com.onlineauction.OnlineAuction.mapper.CategoryMapper;
import com.onlineauction.OnlineAuction.repository.CategoryRepository;
import com.onlineauction.OnlineAuction.service.CategoryService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Validated
@Transactional
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Override
    public CategoryDTO getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .map(categoryMapper::categoryToCategoryDto)
                .orElseThrow(() -> new EntityNotFoundException("Категория с таким ID не найдена: " + id));
    }

    @Override
    public List<CategoryDTO> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(categoryMapper::categoryToCategoryDto)
                .collect(Collectors.toList());
    }

    @Override
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
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryException("Категория с ID " + id + " не существует"));
        boolean hasAssociatedLots = categoryRepository.existsLotsByCategoryId(category);
        if (hasAssociatedLots) {
            throw new CategoryException("Нельзя удалить категорию, так как к ней привязаны лоты");
        }
        categoryRepository.deleteById(id);
    }

    @Override
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
        }).orElseThrow(() -> new EntityNotFoundException("Категория с таким ID не найдена: " + id));
    }
}
