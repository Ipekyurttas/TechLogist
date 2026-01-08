package org.tech.techlogist.dto.order;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderResponseDto {

    private Long orderId;
    private Long userId;
    private String username;
    private LocalDateTime orderDate;
    private String status;
    private List<OrderItemResponseDto> items;
}

