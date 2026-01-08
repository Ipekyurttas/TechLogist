package org.tech.techlogist.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.tech.techlogist.dto.category.CategoryCreateRequestDto;
import org.tech.techlogist.dto.category.CategoryUpdateRequestDto;
import org.tech.techlogist.dto.category.CategoryResponseDto;
import org.tech.techlogist.service.CategoryService;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;


    @PostMapping
    public ResponseEntity<CategoryResponseDto> createCategory(
            @RequestBody CategoryCreateRequestDto dto) {

        CategoryResponseDto createdCategory =
                categoryService.createCategory(dto);

        return new ResponseEntity<>(createdCategory, HttpStatus.CREATED);
    }


    @GetMapping
    public ResponseEntity<List<CategoryResponseDto>> getAllCategories() {
        return ResponseEntity.ok(categoryService.getAllCategories());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponseDto> getCategoryById(
            @PathVariable Long id) {

        return ResponseEntity.ok(categoryService.getCategoryById(id));
    }


    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponseDto> updateCategory(
            @PathVariable Long id,
            @RequestBody CategoryUpdateRequestDto dto) {

        return ResponseEntity.ok(
                categoryService.updateCategory(id, dto)
        );
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(
            @PathVariable Long id) {

        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
}
