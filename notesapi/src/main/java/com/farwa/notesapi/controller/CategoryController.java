package com.farwa.notesapi.controller;

import com.farwa.notesapi.dto.CategoryRequestDto;
import com.farwa.notesapi.dto.CategoryResponseDto;
import com.farwa.notesapi.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }
    @PostMapping
    public CategoryResponseDto createCategory(
            @Valid @RequestBody CategoryRequestDto requestDto
    ) {
        return categoryService.createCategory(requestDto);
    }
    @GetMapping
    public List<CategoryResponseDto> getAllCategories() {
        return categoryService.getAllCategories();
    }
    @GetMapping("/{id}")
    public CategoryResponseDto getCategoryById(
            @PathVariable Long id
    ) {
        return categoryService.getCategoryById(id);
    }
    @PutMapping("/{id}")
    public CategoryResponseDto updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody CategoryRequestDto requestDto
    ) {
        return categoryService.updateCategory(id, requestDto);
    }
    @DeleteMapping("/{id}")
    public void deleteCategory(
            @PathVariable Long id
    ) {
        categoryService.deleteCategory(id);
    }
}