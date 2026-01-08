package org.tech.techlogist.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser; // Kritik ekleme
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.tech.techlogist.dto.order.OrderCreateRequestDto;
import org.tech.techlogist.dto.order.OrderResponseDto;
import org.tech.techlogist.service.OrderService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class OrderControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private OrderService orderService;

    @Test
    @WithMockUser(username = "testuser", roles = {"CUSTOMER"})
    void createOrder_ShouldReturnCreated() throws Exception {
        OrderCreateRequestDto requestDto = new OrderCreateRequestDto();
        OrderResponseDto responseDto = new OrderResponseDto();
        responseDto.setOrderId(1L);
        responseDto.setStatus("CREATED");

        when(orderService.createOrder(any())).thenReturn(responseDto);

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderId").value(1))
                .andExpect(jsonPath("$.status").value("CREATED"));
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"CUSTOMER"})
    void getOrdersByUser_ShouldReturnList() throws Exception {
        OrderResponseDto order = new OrderResponseDto();
        order.setOrderId(10L);
        when(orderService.getOrdersByUser(1L)).thenReturn(List.of(order));

        mockMvc.perform(get("/api/orders/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].orderId").value(10));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void cancelOrder_ShouldReturnNoContent() throws Exception {
        mockMvc.perform(put("/api/orders/5/cancel"))
                .andExpect(status().isNoContent());

        verify(orderService).cancelOrder(5L);
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void markOrderAsPaid_ShouldReturnNoContent() throws Exception {
        mockMvc.perform(put("/api/orders/5/pay"))
                .andExpect(status().isNoContent());

        verify(orderService).markOrderAsPaid(5L);
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"CUSTOMER"})
    void getOrderById_ShouldReturnOrder() throws Exception {
        OrderResponseDto order = new OrderResponseDto();
        order.setOrderId(1L);
        when(orderService.getOrderById(1L)).thenReturn(order);

        mockMvc.perform(get("/api/orders/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(1));
    }
}