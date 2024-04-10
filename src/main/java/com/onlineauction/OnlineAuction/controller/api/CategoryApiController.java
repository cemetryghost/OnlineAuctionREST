package com.onlineauction.OnlineAuction.controller.api;

import com.onlineauction.OnlineAuction.dto.CategoryDTO;
import com.onlineauction.OnlineAuction.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/category")
public class CategoryApiController {

    private final CategoryService categoryService;

    @Autowired
    public CategoryApiController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryDTO> getCategoryById(@Valid @PathVariable Long id) {
        CategoryDTO categoryDTO = categoryService.getCategoryById(id);
        return categoryDTO != null ?
                ResponseEntity.ok(categoryDTO) :
                ResponseEntity.notFound().build();
    }

    @GetMapping
    public ResponseEntity<List<CategoryDTO>> getAllCategories() {
        List<CategoryDTO> categoryDTOList = categoryService.getAllCategories();
        return ResponseEntity.ok(categoryDTOList);
    }

    @PostMapping
    public ResponseEntity<CategoryDTO> createCategory(@Valid @RequestBody CategoryDTO categoryDTO) {
        CategoryDTO createdCategoryDTO = categoryService.createCategory(categoryDTO);
        return new ResponseEntity<>(createdCategoryDTO, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateCategoryName(@Valid @PathVariable Long id, @RequestBody CategoryDTO categoryDTO) {
        if (categoryDTO.getNameCategory() == null || categoryDTO.getNameCategory().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Name category cannot be empty");
        }
        boolean updateResult = categoryService.updateCategoryName(id, categoryDTO.getNameCategory());
        return updateResult ?
                ResponseEntity.ok().build() :
                ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@Valid @PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
}
