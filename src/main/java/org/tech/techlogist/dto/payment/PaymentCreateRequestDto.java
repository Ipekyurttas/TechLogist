package org.tech.techlogist.dto.payment;

import lombok.Data;

@Data
public class PaymentCreateRequestDto {

    private Long orderId;
    private String paymentMethod;
}
