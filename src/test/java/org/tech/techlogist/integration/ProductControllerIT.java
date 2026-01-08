package org.tech.techlogist.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.tech.techlogist.dto.product.ProductCreateRequestDto;
import org.tech.techlogist.dto.product.ProductResponseDto;
import org.tech.techlogist.dto.product.ProductUpdateRequestDto;
import org.tech.techlogist.service.ProductService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ProductControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProductService productService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void createProduct_ShouldReturnCreated() throws Exception {
        ProductCreateRequestDto requestDto = new ProductCreateRequestDto();
        requestDto.setName("Gaming Laptop");
        requestDto.setPrice(45000.0);
        ProductResponseDto responseDto = new ProductResponseDto();
        responseDto.setId(1L);
        responseDto.setName("Gaming Laptop");
        when(productService.createProduct(any())).thenReturn(responseDto);
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Gaming Laptop"));
    }

    @Test
    void getAllProducts_ShouldReturnList() throws Exception {
        ProductResponseDto product = new ProductResponseDto();
        product.setName("Mouse");
        when(productService.getAllProducts()).thenReturn(List.of(product));
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Mouse"));
    }

    @Test
    void getProductById_ShouldReturnProduct() throws Exception {
        ProductResponseDto responseDto = new ProductResponseDto();
        responseDto.setId(10L);
        responseDto.setName("Klavye");
        when(productService.getProductById(10L)).thenReturn(responseDto);
        mockMvc.perform(get("/api/products/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.name").value("Klavye"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateProduct_ShouldReturnUpdatedProduct() throws Exception {
        ProductUpdateRequestDto updateDto = new ProductUpdateRequestDto();
        updateDto.setName("Güncellenmiş Ürün");
        ProductResponseDto responseDto = new ProductResponseDto();
        responseDto.setId(10L);
        responseDto.setName("Güncellenmiş Ürün");
        when(productService.updateProduct(eq(10L), any())).thenReturn(responseDto);
        mockMvc.perform(put("/api/products/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Güncellenmiş Ürün"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteProduct_ShouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/api/products/1"))
                .andExpect(status().isNoContent());

        verify(productService).deleteProduct(1L);
    }
}