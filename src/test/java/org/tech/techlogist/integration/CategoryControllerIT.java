package org.tech.techlogist.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.tech.techlogist.dto.category.CategoryCreateRequestDto;
import org.tech.techlogist.dto.category.CategoryResponseDto;
import org.tech.techlogist.dto.category.CategoryUpdateRequestDto;
import org.tech.techlogist.service.CategoryService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class CategoryControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CategoryService categoryService;

    @Test
    void createCategory_ShouldReturnCreated() throws Exception {
        CategoryCreateRequestDto requestDto = new CategoryCreateRequestDto();
        requestDto.setName("Telefon");
        CategoryResponseDto responseDto = new CategoryResponseDto();
        responseDto.setId(1L);
        responseDto.setName("Telefon");
        when(categoryService.createCategory(any())).thenReturn(responseDto);
        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Telefon"));
    }

    @Test
    void getAllCategories_ShouldReturnList() throws Exception {
        CategoryResponseDto cat1 = new CategoryResponseDto();
        cat1.setName("Laptop");
        when(categoryService.getAllCategories()).thenReturn(List.of(cat1));
        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Laptop"));
    }

    @Test
    void getCategoryById_ShouldReturnCategory() throws Exception {
        CategoryResponseDto responseDto = new CategoryResponseDto();
        responseDto.setId(5L);
        responseDto.setName("Tablet");
        when(categoryService.getCategoryById(5L)).thenReturn(responseDto);
        mockMvc.perform(get("/api/categories/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Tablet"));
    }

    @Test
    void updateCategory_ShouldReturnUpdatedCategory() throws Exception {
        CategoryUpdateRequestDto updateDto = new CategoryUpdateRequestDto();
        updateDto.setName("Yeni İsim");
        CategoryResponseDto responseDto = new CategoryResponseDto();
        responseDto.setId(5L);
        responseDto.setName("Yeni İsim");
        when(categoryService.updateCategory(eq(5L), any())).thenReturn(responseDto);
        mockMvc.perform(put("/api/categories/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Yeni İsim"));
    }

    @Test
    void deleteCategory_ShouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/api/categories/10"))
                .andExpect(status().isNoContent());

        verify(categoryService).deleteCategory(10L);
    }
}