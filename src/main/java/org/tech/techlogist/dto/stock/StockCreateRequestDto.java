package org.tech.techlogist.dto.stock;


import lombok.Data;

@Data
public class StockCreateRequestDto {

    private Long productId;
    private int quantity;
    private int minQuantity;
}

