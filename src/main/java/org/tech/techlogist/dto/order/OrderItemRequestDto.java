package org.tech.techlogist.dto.order;

import lombok.Data;

@Data
public class OrderItemRequestDto {

    private Long productId;
    private int quantity;
}

