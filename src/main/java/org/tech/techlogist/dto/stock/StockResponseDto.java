package org.tech.techlogist.dto.stock;

import lombok.Data;

@Data
public class StockResponseDto {

    private Long id;
    private Long productId;
    private String productName;
    private int quantity;
    private int minQuantity;
}

