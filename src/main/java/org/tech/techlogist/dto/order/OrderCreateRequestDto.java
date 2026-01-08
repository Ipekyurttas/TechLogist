package org.tech.techlogist.dto.order;

import lombok.Data;

import java.util.List;

@Data
public class OrderCreateRequestDto {

    private Long userId;
    private List<OrderItemRequestDto> items;
}

