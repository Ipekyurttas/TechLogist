package org.tech.techlogist.dto.payment;

import lombok.Data;

@Data
public class PaymentResponseDto {

    private Long id;
    private Long orderId;
    private String paymentMethod;
    private String status;
}

