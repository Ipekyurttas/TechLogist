package org.tech.techlogist.dto.product;

import lombok.Data;

@Data
public class ProductCreateRequestDto {

    private String name;
    private String description;
    private double price;
    private Long categoryId;
    private Integer stockQuantity;
    private Integer minStockQuantity;
}
