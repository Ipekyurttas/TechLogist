package org.tech.techlogist.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.tech.techlogist.dto.payment.PaymentCreateRequestDto;
import org.tech.techlogist.dto.payment.PaymentResponseDto;
import org.tech.techlogist.service.PaymentService;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;


    @PostMapping
    public ResponseEntity<PaymentResponseDto> payOrder(
            @RequestBody PaymentCreateRequestDto dto) {

        PaymentResponseDto paymentResponse = paymentService.payOrder(dto);
        return new ResponseEntity<>(paymentResponse, HttpStatus.CREATED);
    }


    @GetMapping("/order/{orderId}")
    public ResponseEntity<PaymentResponseDto> getPaymentByOrderId(
            @PathVariable Long orderId) {

        return ResponseEntity.ok(
                paymentService.getPaymentByOrderId(orderId)
        );
    }
}
