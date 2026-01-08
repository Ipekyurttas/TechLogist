package org.tech.techlogist.unit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.tech.techlogist.dto.category.CategoryCreateRequestDto;
import org.tech.techlogist.dto.category.CategoryResponseDto;
import org.tech.techlogist.dto.category.CategoryUpdateRequestDto;
import org.tech.techlogist.entity.Category;
import org.tech.techlogist.repository.CategoryRepository;
import org.tech.techlogist.service.CategoryService;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    private Category category;
    private CategoryResponseDto responseDto;

    @BeforeEach
    void setUp() {
        category = new Category();
        category.setId(1L);
        category.setName("Elektronik");
    }

    @Test
    void createCategory_WhenNameDoesNotExist_ShouldReturnResponseDto() {
        CategoryCreateRequestDto requestDto = new CategoryCreateRequestDto();
        requestDto.setName("Elektronik");
        when(categoryRepository.findByName(requestDto.getName())).thenReturn(Optional.empty());
        when(categoryRepository.save(any(Category.class))).thenReturn(category);
        CategoryResponseDto result = categoryService.createCategory(requestDto);
        assertNotNull(result);
        assertEquals(category.getName(), result.getName());
        verify(categoryRepository, times(1)).save(any(Category.class));
    }

    @Test
    void createCategory_WhenNameAlreadyExists_ShouldThrowRuntimeException() {
        CategoryCreateRequestDto requestDto = new CategoryCreateRequestDto();
        requestDto.setName("Elektronik");
        when(categoryRepository.findByName(requestDto.getName())).thenReturn(Optional.of(category));
        assertThrows(RuntimeException.class, () -> categoryService.createCategory(requestDto));
        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    void getAllCategories_ShouldReturnList() {
        when(categoryRepository.findAll()).thenReturn(List.of(category));
        List<CategoryResponseDto> result = categoryService.getAllCategories();
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(category.getName(), result.get(0).getName());
    }

    @Test
    void getCategoryById_WhenIdExists_ShouldReturnResponseDto() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        CategoryResponseDto result = categoryService.getCategoryById(1L);
        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void getCategoryById_WhenIdDoesNotExist_ShouldThrowRuntimeException() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> categoryService.getCategoryById(1L));
    }

    @Test
    void updateCategory_WhenValidRequest_ShouldUpdateAndReturnDto() {
        CategoryUpdateRequestDto updateDto = new CategoryUpdateRequestDto();
        updateDto.setName("Yeni İsim");
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(categoryRepository.findByName("Yeni İsim")).thenReturn(Optional.empty());
        when(categoryRepository.save(any(Category.class))).thenReturn(category);
        CategoryResponseDto result = categoryService.updateCategory(1L, updateDto);
        assertNotNull(result);
        verify(categoryRepository).save(category);
    }

    @Test
    void deleteCategory_WhenIdExists_ShouldCallDelete() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        categoryService.deleteCategory(1L);
        verify(categoryRepository, times(1)).delete(category);
    }
}
