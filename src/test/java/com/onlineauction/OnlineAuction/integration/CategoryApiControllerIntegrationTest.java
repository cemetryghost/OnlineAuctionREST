package com.onlineauction.OnlineAuction.integration;

import com.onlineauction.OnlineAuction.config.TestSecurityConfig;
import com.onlineauction.OnlineAuction.dto.CategoryDTO;
import com.onlineauction.OnlineAuction.service.CategoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
class CategoryApiControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @MockBean
    private CategoryService categoryService;

    private CategoryDTO categoryDTO;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        categoryDTO = new CategoryDTO();
        categoryDTO.setId(1L);
        categoryDTO.setNameCategory("Electronics");
    }

    @Test
    void testGetCategoryById_Success() throws Exception {
        when(categoryService.getCategoryById(anyLong())).thenReturn(categoryDTO);

        mockMvc.perform(get("/category/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(categoryDTO.getId()))
                .andExpect(jsonPath("$.nameCategory").value(categoryDTO.getNameCategory()));

        verify(categoryService, times(1)).getCategoryById(1L);
    }

    @Test
    void testGetCategoryById_NotFound() throws Exception {
        when(categoryService.getCategoryById(anyLong())).thenReturn(null);

        mockMvc.perform(get("/category/{id}", 1L))
                .andExpect(status().isNotFound());

        verify(categoryService, times(1)).getCategoryById(1L);
    }

    @Test
    void testGetAllCategories() throws Exception {
        List<CategoryDTO> categories = Arrays.asList(categoryDTO);
        when(categoryService.getAllCategories()).thenReturn(categories);

        mockMvc.perform(get("/category"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(categoryDTO.getId()))
                .andExpect(jsonPath("$[0].nameCategory").value(categoryDTO.getNameCategory()));

        verify(categoryService, times(1)).getAllCategories();
    }

    @Test
    void testCreateCategory() throws Exception {
        when(categoryService.createCategory(any(CategoryDTO.class))).thenReturn(categoryDTO);

        mockMvc.perform(post("/category")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nameCategory\":\"Electronics\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(categoryDTO.getId()))
                .andExpect(jsonPath("$.nameCategory").value(categoryDTO.getNameCategory()));

        verify(categoryService, times(1)).createCategory(any(CategoryDTO.class));
    }

    @Test
    void testUpdateCategoryName_Success() throws Exception {
        when(categoryService.updateCategoryName(anyLong(), anyString())).thenReturn(true);

        mockMvc.perform(put("/category/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nameCategory\":\"Home Appliances\"}"))
                .andExpect(status().isOk());

        verify(categoryService, times(1)).updateCategoryName(eq(1L), anyString());
    }

    @Test
    void testUpdateCategoryName_NotFound() throws Exception {
        when(categoryService.updateCategoryName(anyLong(), anyString())).thenReturn(false);

        mockMvc.perform(put("/category/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nameCategory\":\"Home Appliances\"}"))
                .andExpect(status().isNotFound());

        verify(categoryService, times(1)).updateCategoryName(eq(1L), anyString());
    }

    @Test
    void testDeleteCategory() throws Exception {
        doNothing().when(categoryService).deleteCategory(anyLong());

        mockMvc.perform(delete("/category/{id}", 1L))
                .andExpect(status().isNoContent());

        verify(categoryService, times(1)).deleteCategory(1L);
    }
}