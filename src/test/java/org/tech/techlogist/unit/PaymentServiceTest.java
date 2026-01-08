package org.tech.techlogist.unit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.tech.techlogist.dto.payment.PaymentCreateRequestDto;
import org.tech.techlogist.dto.payment.PaymentResponseDto;
import org.tech.techlogist.entity.Order;
import org.tech.techlogist.entity.Payment;
import org.tech.techlogist.repository.OrderRepository;
import org.tech.techlogist.repository.PaymentRepository;
import org.tech.techlogist.service.PaymentService;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private PaymentService paymentService;

    private Order order;
    private Payment payment;
    private PaymentCreateRequestDto requestDto;

    @BeforeEach
    void setUp() {
        order = new Order();
        order.setId(1L);
        order.setStatus("CREATED");

        payment = new Payment();
        payment.setId(10L);
        payment.setOrder(order);
        payment.setMethod("CREDIT_CARD");
        payment.setStatus("SUCCESS");

        requestDto = new PaymentCreateRequestDto();
        requestDto.setOrderId(1L);
        requestDto.setPaymentMethod("CREDIT_CARD");
    }

    @Test
    void payOrder_SuccessfulScenario() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(paymentRepository.findByOrderId(1L)).thenReturn(Optional.empty());
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);
        PaymentResponseDto result = paymentService.payOrder(requestDto);
        assertNotNull(result);
        assertEquals("SUCCESS", result.getStatus());
        assertEquals("PAID", order.getStatus());
        verify(orderRepository).save(order);
        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    void payOrder_WhenOrderIdIsNull_ShouldThrowException() {
        requestDto.setOrderId(null);
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                paymentService.payOrder(requestDto));
        assertEquals("Ödeme işlemi için Sipariş ID (orderId) boş olamaz!", exception.getMessage());
    }

    @Test
    void payOrder_WhenOrderNotFound_ShouldThrowException() {
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> paymentService.payOrder(requestDto));
    }

    @Test
    void payOrder_WhenPaymentAlreadyExists_ShouldThrowException() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(paymentRepository.findByOrderId(1L)).thenReturn(Optional.of(payment));
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                paymentService.payOrder(requestDto));
        assertEquals("Payment already exists for this order", exception.getMessage());
    }

    @Test
    void payOrder_WhenOrderStatusIsNotCreated_ShouldThrowException() {
        order.setStatus("SHIPPED");
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(paymentRepository.findByOrderId(1L)).thenReturn(Optional.empty());
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                paymentService.payOrder(requestDto));
        assertEquals("Only CREATED orders can be paid", exception.getMessage());
    }

    @Test
    void getPaymentByOrderId_ShouldReturnResponseDto() {
        when(paymentRepository.findByOrderId(1L)).thenReturn(Optional.of(payment));
        PaymentResponseDto result = paymentService.getPaymentByOrderId(1L);
        assertNotNull(result);
        assertEquals(10L, result.getId());
    }
}
