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
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CategoryMapper categoryMapper;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    private Category category;
    private CategoryDTO categoryDTO;

    @BeforeEach
    public void setUp() {
        category = new Category();
        category.setId(1L);
        category.setNameCategory("Electronics");

        categoryDTO = new CategoryDTO();
        categoryDTO.setId(1L);
        categoryDTO.setNameCategory("Electronics");
    }

    @Test
    public void testGetCategoryById() {
        when(categoryRepository.findById(anyLong())).thenReturn(Optional.of(category));
        when(categoryMapper.categoryToCategoryDto(any(Category.class))).thenReturn(categoryDTO);

        CategoryDTO result = categoryService.getCategoryById(1L);
        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    public void testGetCategoryByIdNotFound() {
        when(categoryRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> categoryService.getCategoryById(1L));
    }

    @Test
    public void testGetAllCategories() {
        when(categoryRepository.findAll()).thenReturn(Arrays.asList(category));
        when(categoryMapper.categoryToCategoryDto(any(Category.class))).thenReturn(categoryDTO);

        List<CategoryDTO> result = categoryService.getAllCategories();
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Electronics", result.get(0).getNameCategory());
    }

    @Test
    public void testCreateCategory() {
        when(categoryRepository.existsByNameCategory(anyString())).thenReturn(false);
        when(categoryMapper.categoryDtoToCategory(any(CategoryDTO.class))).thenReturn(category);
        when(categoryRepository.save(any(Category.class))).thenReturn(category);
        when(categoryMapper.categoryToCategoryDto(any(Category.class))).thenReturn(categoryDTO);

        CategoryDTO result = categoryService.createCategory(categoryDTO);
        assertNotNull(result);
        assertEquals("Electronics", result.getNameCategory());
    }

    @Test
    public void testCreateCategory_NameExists() {
        when(categoryRepository.existsByNameCategory(anyString())).thenReturn(true);

        assertThrows(CategoryException.class, () -> categoryService.createCategory(categoryDTO));
    }

    @Test
    public void testDeleteCategory() {
        when(categoryRepository.findById(anyLong())).thenReturn(Optional.of(category));
        when(categoryRepository.existsLotsByCategoryId(any(Category.class))).thenReturn(false);
        doNothing().when(categoryRepository).deleteById(anyLong());

        categoryService.deleteCategory(1L);

        verify(categoryRepository, times(1)).deleteById(1L);
    }

    @Test
    public void testDeleteCategory_WithLots() {
        when(categoryRepository.findById(anyLong())).thenReturn(Optional.of(category));
        when(categoryRepository.existsLotsByCategoryId(any(Category.class))).thenReturn(true);

        assertThrows(CategoryException.class, () -> categoryService.deleteCategory(1L));
    }

    @Test
    public void testUpdateCategoryName() {
        when(categoryRepository.findById(anyLong())).thenReturn(Optional.of(category));
        when(categoryRepository.existsByNameCategory(anyString())).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenReturn(category);

        boolean result = categoryService.updateCategoryName(1L, "Updated Electronics");
        assertTrue(result);
        assertEquals("Updated Electronics", category.getNameCategory());
    }

    @Test
    public void testUpdateCategoryName_NameExists() {
        when(categoryRepository.findById(anyLong())).thenReturn(Optional.of(category));
        when(categoryRepository.existsByNameCategory(anyString())).thenReturn(true);

        assertThrows(CategoryException.class, () -> categoryService.updateCategoryName(1L, "Updated Electronics"));
    }
}