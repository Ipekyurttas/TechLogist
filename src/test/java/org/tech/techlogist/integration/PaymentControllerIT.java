package org.tech.techlogist.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser; // Kimlik simülasyonu için gerekli
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.tech.techlogist.dto.payment.PaymentCreateRequestDto;
import org.tech.techlogist.dto.payment.PaymentResponseDto;
import org.tech.techlogist.service.PaymentService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PaymentControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PaymentService paymentService;

    @Test
    @WithMockUser(username = "testuser", roles = {"CUSTOMER"})
    void payOrder_ShouldReturnCreated() throws Exception {
        PaymentCreateRequestDto requestDto = new PaymentCreateRequestDto();
        requestDto.setOrderId(1L);
        requestDto.setPaymentMethod("CREDIT_CARD");

        PaymentResponseDto responseDto = new PaymentResponseDto();
        responseDto.setId(10L);
        responseDto.setOrderId(1L);
        responseDto.setStatus("SUCCESS");

        when(paymentService.payOrder(any(PaymentCreateRequestDto.class))).thenReturn(responseDto);

        mockMvc.perform(post("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"CUSTOMER"})
    void getPaymentByOrderId_ShouldReturnPayment() throws Exception {
        PaymentResponseDto responseDto = new PaymentResponseDto();
        responseDto.setOrderId(1L);
        responseDto.setPaymentMethod("PAYPAL");

        when(paymentService.getPaymentByOrderId(1L)).thenReturn(responseDto);

        mockMvc.perform(get("/api/payments/order/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentMethod").value("PAYPAL"));
    }
}