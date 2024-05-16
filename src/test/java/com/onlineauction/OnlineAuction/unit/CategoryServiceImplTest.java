package com.onlineauction.OnlineAuction.unit;

import com.onlineauction.OnlineAuction.dto.CategoryDTO;
import com.onlineauction.OnlineAuction.entity.Category;
import com.onlineauction.OnlineAuction.exception.CategoryException;
import com.onlineauction.OnlineAuction.mapper.CategoryMapper;
import com.onlineauction.OnlineAuction.repository.CategoryRepository;
import com.onlineauction.OnlineAuction.service.impl.CategoryServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CategoryMapper categoryMapper;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetCategoryById_Success() {
        Long categoryId = 1L;
        Category category = new Category();
        category.setId(categoryId);
        CategoryDTO categoryDTO = new CategoryDTO();
        categoryDTO.setId(categoryId);

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(categoryMapper.categoryToCategoryDto(category)).thenReturn(categoryDTO);

        CategoryDTO result = categoryService.getCategoryById(categoryId);

        assertNotNull(result);
        assertEquals(categoryId, result.getId());
    }

    @Test
    void testGetCategoryById_NotFound() {
        Long categoryId = 1L;

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> categoryService.getCategoryById(categoryId));

        assertEquals("Категория с таким ID не найдена: " + categoryId, exception.getMessage());
    }

    @Test
    void testGetAllCategories_Success() {
        Category category1 = new Category();
        Category category2 = new Category();
        CategoryDTO categoryDTO1 = new CategoryDTO();
        CategoryDTO categoryDTO2 = new CategoryDTO();

        when(categoryRepository.findAll()).thenReturn(Arrays.asList(category1, category2));
        when(categoryMapper.categoryToCategoryDto(category1)).thenReturn(categoryDTO1);
        when(categoryMapper.categoryToCategoryDto(category2)).thenReturn(categoryDTO2);

        List<CategoryDTO> result = categoryService.getAllCategories();

        assertEquals(2, result.size());
    }

    @Test
    void testCreateCategory_Success() {
        CategoryDTO categoryDTO = new CategoryDTO();
        categoryDTO.setNameCategory("New Category");
        Category category = new Category();
        category.setNameCategory("New Category");

        when(categoryRepository.existsByNameCategory(categoryDTO.getNameCategory())).thenReturn(false);
        when(categoryMapper.categoryDtoToCategory(categoryDTO)).thenReturn(category);
        when(categoryRepository.save(category)).thenReturn(category);
        when(categoryMapper.categoryToCategoryDto(category)).thenReturn(categoryDTO);

        CategoryDTO result = categoryService.createCategory(categoryDTO);

        assertNotNull(result);
        assertEquals(categoryDTO.getNameCategory(), result.getNameCategory());
    }

    @Test
    void testCreateCategory_EmptyName() {
        CategoryDTO categoryDTO = new CategoryDTO();
        categoryDTO.setNameCategory("");

        CategoryException exception = assertThrows(CategoryException.class, () -> categoryService.createCategory(categoryDTO));

        assertEquals("Имя категории не может быть пустым", exception.getMessage());
    }

    @Test
    void testCreateCategory_DuplicateName() {
        CategoryDTO categoryDTO = new CategoryDTO();
        categoryDTO.setNameCategory("Duplicate Category");

        when(categoryRepository.existsByNameCategory(categoryDTO.getNameCategory())).thenReturn(true);

        CategoryException exception = assertThrows(CategoryException.class, () -> categoryService.createCategory(categoryDTO));

        assertEquals("Категория с именем \"Duplicate Category\" уже существует", exception.getMessage());
    }

    @Test
    void testDeleteCategory_Success() {
        Long categoryId = 1L;
        Category category = new Category();
        category.setId(categoryId);

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(categoryRepository.existsLotsByCategoryId(category)).thenReturn(false);

        categoryService.deleteCategory(categoryId);

        verify(categoryRepository, times(1)).deleteById(categoryId);
    }

    @Test
    void testDeleteCategory_NotFound() {
        Long categoryId = 1L;

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        CategoryException exception = assertThrows(CategoryException.class, () -> categoryService.deleteCategory(categoryId));

        assertEquals("Категория с ID " + categoryId + " не существует", exception.getMessage());
    }

    @Test
    void testDeleteCategory_WithLots() {
        Long categoryId = 1L;
        Category category = new Category();
        category.setId(categoryId);

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(categoryRepository.existsLotsByCategoryId(category)).thenReturn(true);

        CategoryException exception = assertThrows(CategoryException.class, () -> categoryService.deleteCategory(categoryId));

        assertEquals("Нельзя удалить категорию, так как к ней привязаны лоты", exception.getMessage());
    }

    @Test
    void testUpdateCategoryName_Success() {
        Long categoryId = 1L;
        String newName = "Updated Category";
        Category category = new Category();
        category.setId(categoryId);
        category.setNameCategory("Old Category");

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(categoryRepository.existsByNameCategory(newName)).thenReturn(false);
        when(categoryRepository.save(category)).thenReturn(category);

        boolean result = categoryService.updateCategoryName(categoryId, newName);

        assertTrue(result);
        assertEquals(newName, category.getNameCategory());
    }

    @Test
    void testUpdateCategoryName_EmptyName() {
        Long categoryId = 1L;

        CategoryException exception = assertThrows(CategoryException.class, () -> categoryService.updateCategoryName(categoryId, ""));

        assertEquals("Имя категории не может быть пустым", exception.getMessage());
    }

    @Test
    void testUpdateCategoryName_DuplicateName() {
        Long categoryId = 1L;
        String newName = "Duplicate Category";
        Category category = new Category();
        category.setId(categoryId);
        category.setNameCategory("Old Category");

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(categoryRepository.existsByNameCategory(newName)).thenReturn(true);

        CategoryException exception = assertThrows(CategoryException.class, () -> categoryService.updateCategoryName(categoryId, newName));

        assertEquals("Категория с именем \"" + newName + "\" уже существует", exception.getMessage());
    }

    @Test
    void testUpdateCategoryName_NotFound() {
        Long categoryId = 1L;
        String newName = "Updated Category";

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> categoryService.updateCategoryName(categoryId, newName));

        assertEquals("Категория с таким ID не найдена: " + categoryId, exception.getMessage());
    }
}

