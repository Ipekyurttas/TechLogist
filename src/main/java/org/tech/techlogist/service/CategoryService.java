package org.tech.techlogist.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.tech.techlogist.dto.category.CategoryCreateRequestDto;
import org.tech.techlogist.dto.category.CategoryUpdateRequestDto;
import org.tech.techlogist.dto.category.CategoryResponseDto;
import org.tech.techlogist.entity.Category;
import org.tech.techlogist.repository.CategoryRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;


    public CategoryResponseDto createCategory(CategoryCreateRequestDto dto) {

        if (categoryRepository.findByName(dto.getName()).isPresent()) {
            throw new RuntimeException("Category already exists");
        }

        Category category = new Category();
        category.setName(dto.getName());

        return mapToResponse(categoryRepository.save(category));
    }


    public List<CategoryResponseDto> getAllCategories() {
        return categoryRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public CategoryResponseDto getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));
        return mapToResponse(category);
    }


    public CategoryResponseDto updateCategory(Long id, CategoryUpdateRequestDto dto) {

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        if (!category.getName().equals(dto.getName())
                && categoryRepository.findByName(dto.getName()).isPresent()) {
            throw new RuntimeException("Category name already exists");
        }

        category.setName(dto.getName());
        return mapToResponse(categoryRepository.save(category));
    }


    public void deleteCategory(Long id) {

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        categoryRepository.delete(category);
    }


    private CategoryResponseDto mapToResponse(Category category) {
        CategoryResponseDto dto = new CategoryResponseDto();
        dto.setId(category.getId());
        dto.setName(category.getName());
        return dto;
    }
}
