package org.tech.techlogist.dto.order;

import lombok.Data;

@Data
public class OrderItemResponseDto {

    private Long productId;
    private String productName;
    private int quantity;
    private double price;
}

