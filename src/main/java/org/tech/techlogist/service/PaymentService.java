package org.tech.techlogist.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.tech.techlogist.dto.payment.PaymentCreateRequestDto;
import org.tech.techlogist.dto.payment.PaymentResponseDto;
import org.tech.techlogist.entity.Order;
import org.tech.techlogist.entity.Payment;
import org.tech.techlogist.repository.OrderRepository;
import org.tech.techlogist.repository.PaymentRepository;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;


    @Transactional
    public PaymentResponseDto payOrder(PaymentCreateRequestDto dto) {

        if (dto.getOrderId() == null) {
            throw new RuntimeException("Ödeme işlemi için Sipariş ID (orderId) boş olamaz!");
        }

        Order order = orderRepository.findById(dto.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (paymentRepository.findByOrderId(dto.getOrderId()).isPresent()) {
            throw new RuntimeException("Payment already exists for this order");
        }

        if (!order.getStatus().equals("CREATED")) {
            throw new RuntimeException("Only CREATED orders can be paid");
        }

        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setMethod(dto.getPaymentMethod());
        payment.setStatus("SUCCESS");

        Payment savedPayment = paymentRepository.save(payment);

        order.setStatus("PAID");
        orderRepository.save(order);

        return mapToResponse(savedPayment);
    }


    public PaymentResponseDto getPaymentByOrderId(Long orderId) {

        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        return mapToResponse(payment);
    }


    private PaymentResponseDto mapToResponse(Payment payment) {

        PaymentResponseDto dto = new PaymentResponseDto();
        dto.setId(payment.getId());
        dto.setOrderId(payment.getOrder().getId());
        dto.setPaymentMethod(payment.getMethod());
        dto.setStatus(payment.getStatus());

        return dto;
    }
}
