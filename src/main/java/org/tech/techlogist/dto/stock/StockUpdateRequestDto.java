package org.tech.techlogist.dto.stock;


import lombok.Data;

@Data
public class StockUpdateRequestDto {

    private int quantity;
    private int minQuantity;
}
