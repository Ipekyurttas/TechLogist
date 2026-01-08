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
import org.tech.techlogist.dto.stock.StockCreateRequestDto;
import org.tech.techlogist.dto.stock.StockResponseDto;
import org.tech.techlogist.service.StockService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class StockControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private StockService stockService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void createStock_ShouldReturnCreated() throws Exception {
        StockCreateRequestDto requestDto = new StockCreateRequestDto();
        requestDto.setProductId(1L);
        requestDto.setQuantity(100);
        StockResponseDto responseDto = new StockResponseDto();
        responseDto.setId(10L);
        responseDto.setQuantity(100);
        when(stockService.createStock(any(StockCreateRequestDto.class))).thenReturn(responseDto);
        mockMvc.perform(post("/api/stocks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.quantity").value(100));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getStockByProductId_ShouldReturnStock() throws Exception {
        StockResponseDto responseDto = new StockResponseDto();
        responseDto.setProductId(1L);
        responseDto.setQuantity(50);
        when(stockService.getStockByProductId(1L)).thenReturn(responseDto);
        mockMvc.perform(get("/api/stocks/product/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(50));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void increaseStock_ShouldReturnUpdatedStock() throws Exception {
        StockResponseDto responseDto = new StockResponseDto();
        responseDto.setQuantity(120); // 100 + 20
        when(stockService.increaseStock(eq(1L), eq(20))).thenReturn(responseDto);
        mockMvc.perform(put("/api/stocks/product/1/increase")
                        .param("quantity", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(120));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void decreaseStock_ShouldReturnUpdatedStock() throws Exception {
        StockResponseDto responseDto = new StockResponseDto();
        responseDto.setQuantity(80);
        when(stockService.decreaseStock(eq(1L), eq(20))).thenReturn(responseDto);
        mockMvc.perform(put("/api/stocks/product/1/decrease")
                        .param("quantity", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(80));
    }
}