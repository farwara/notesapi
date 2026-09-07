package com.farwa.notesapi.service;

import com.farwa.notesapi.dto.CategoryRequestDto;
import com.farwa.notesapi.dto.CategoryResponseDto;
import com.farwa.notesapi.exception.ResourceNotFoundException;
import com.farwa.notesapi.model.Category;
import com.farwa.notesapi.repository.CategoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {
    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }
    public CategoryResponseDto createCategory(CategoryRequestDto requestDto) {

        Category category = new Category();
        category.setName(requestDto.getName());

        Category savedCategory = categoryRepository.save(category);

        return mapToResponse(savedCategory);
    }
    public CategoryResponseDto updateCategory(
            Long id,
            CategoryRequestDto requestDto
    ) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Category not found")
                );

        category.setName(requestDto.getName());

        Category updatedCategory = categoryRepository.save(category);

        return mapToResponse(updatedCategory);
    }

    private CategoryResponseDto mapToResponse(Category category) {

        CategoryResponseDto responseDto = new CategoryResponseDto();

        responseDto.setId(category.getId());
        responseDto.setName(category.getName());

        return responseDto;
    }
    public CategoryResponseDto getCategoryById(Long id) {

        Category category = categoryRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Category not found")
                );

        return mapToResponse(category);
    }
    public List<CategoryResponseDto> getAllCategories() {

        return categoryRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }
    public void deleteCategory(Long id) {

        Category category = categoryRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Category not found")
                );

        categoryRepository.delete(category);
    }
    }
