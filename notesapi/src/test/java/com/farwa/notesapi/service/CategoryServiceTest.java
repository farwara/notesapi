
package com.farwa.notesapi.service;

import com.farwa.notesapi.dto.CategoryRequestDto;
import com.farwa.notesapi.dto.CategoryResponseDto;
import com.farwa.notesapi.exception.ResourceNotFoundException;
import com.farwa.notesapi.model.Category;
import com.farwa.notesapi.repository.CategoryRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    private Category createCategory(Long id, String name) {
        Category category = new Category();
        category.setId(id);
        category.setName(name);
        return category;
    }

    private CategoryRequestDto createRequest(String name) {
        CategoryRequestDto request = new CategoryRequestDto();
        request.setName(name);
        return request;
    }

    @Test
    void createCategory_success() {
        CategoryRequestDto request = createRequest("Work");
        Category saved = createCategory(1L, "Work");

        when(categoryRepository.save(any(Category.class)))
                .thenReturn(saved);

        CategoryResponseDto result =
                categoryService.createCategory(request);

        assertEquals(1L, result.getId());
        assertEquals("Work", result.getName());

        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void updateCategory_success() {
        Category category = createCategory(1L, "Old");
        CategoryRequestDto request = createRequest("New");

        when(categoryRepository.findById(1L))
                .thenReturn(Optional.of(category));
        when(categoryRepository.save(category))
                .thenReturn(category);

        CategoryResponseDto result =
                categoryService.updateCategory(1L, request);

        assertEquals(1L, result.getId());
        assertEquals("New", result.getName());

        verify(categoryRepository).save(category);
    }

    @Test
    void updateCategory_notFound() {
        when(categoryRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> categoryService.updateCategory(
                        99L, createRequest("New")));

        verify(categoryRepository, never())
                .save(any(Category.class));
    }

    @Test
    void getCategoryById_success() {
        Category category = createCategory(1L, "Personal");

        when(categoryRepository.findById(1L))
                .thenReturn(Optional.of(category));

        CategoryResponseDto result =
                categoryService.getCategoryById(1L);

        assertEquals(1L, result.getId());
        assertEquals("Personal", result.getName());
    }

    @Test
    void getCategoryById_notFound() {
        when(categoryRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> categoryService.getCategoryById(99L));
    }

    @Test
    void getAllCategories_success() {
        Category first = createCategory(1L, "Work");
        Category second = createCategory(2L, "Personal");

        when(categoryRepository.findAll())
                .thenReturn(List.of(first, second));

        List<CategoryResponseDto> result =
                categoryService.getAllCategories();

        assertEquals(2, result.size());
        assertEquals("Work", result.get(0).getName());
        assertEquals("Personal", result.get(1).getName());
    }

    @Test
    void getAllCategories_empty() {
        when(categoryRepository.findAll())
                .thenReturn(List.of());

        List<CategoryResponseDto> result =
                categoryService.getAllCategories();

        assertTrue(result.isEmpty());
    }

    @Test
    void deleteCategory_success() {
        Category category = createCategory(1L, "Work");

        when(categoryRepository.findById(1L))
                .thenReturn(Optional.of(category));

        categoryService.deleteCategory(1L);

        verify(categoryRepository).delete(category);
    }

    @Test
    void deleteCategory_notFound() {
        when(categoryRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> categoryService.deleteCategory(99L));

        verify(categoryRepository, never())
                .delete(any(Category.class));
    }
}
