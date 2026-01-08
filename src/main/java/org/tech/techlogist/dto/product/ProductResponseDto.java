package org.tech.techlogist.dto.product;

import lombok.Data;

@Data
public class ProductResponseDto {

    private Long id;
    private String name;
    private String description;
    private double price;
    private Long categoryId;
    private String categoryName;
    private Integer stockQuantity;
    private Integer minStockQuantity;
}
