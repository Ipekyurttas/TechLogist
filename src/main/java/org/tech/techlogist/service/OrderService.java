package org.tech.techlogist.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.tech.techlogist.dto.order.*;
import org.tech.techlogist.entity.Order;
import org.tech.techlogist.entity.OrderItem;
import org.tech.techlogist.entity.Product;
import org.tech.techlogist.entity.User;
import org.tech.techlogist.repository.OrderItemRepository;
import org.tech.techlogist.repository.OrderRepository;
import org.tech.techlogist.repository.ProductRepository;
import org.tech.techlogist.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final StockService stockService;


    @Transactional
    public OrderResponseDto createOrder(OrderCreateRequestDto dto) {

        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (dto.getItems() == null || dto.getItems().isEmpty()) {
            throw new RuntimeException("Order must contain at least one product");
        }

        Order order = new Order();
        order.setUser(user);
        order.setOrderDate(LocalDateTime.now());
        order.setStatus("CREATED");

        Order savedOrder = orderRepository.save(order);

        for (OrderItemRequestDto itemDto : dto.getItems()) {

            if (itemDto.getQuantity() <= 0) {
                throw new RuntimeException("Quantity must be greater than zero");
            }

            Product product = productRepository.findById(itemDto.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found"));

            // Stok düş
            stockService.decreaseStock(product.getId(), itemDto.getQuantity());

            OrderItem item = new OrderItem();
            item.setOrder(savedOrder);
            item.setProduct(product);
            item.setQuantity(itemDto.getQuantity());
            item.setPrice(product.getPrice());

            orderItemRepository.save(item);
        }

        return mapToResponse(savedOrder);
    }


    public List<OrderResponseDto> getAllOrders() {
        return orderRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public OrderResponseDto getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        return mapToResponse(order);
    }

    public List<OrderResponseDto> getOrdersByUser(Long userId) {
        return orderRepository.findByUserId(userId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }


    @Transactional
    public void cancelOrder(Long orderId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (!order.getStatus().equals("CREATED")) {
            throw new RuntimeException("Only CREATED orders can be cancelled");
        }

        List<OrderItem> items = orderItemRepository.findByOrderId(orderId);

        for (OrderItem item : items) {
            stockService.increaseStock(
                    item.getProduct().getId(),
                    item.getQuantity()
            );
        }

        order.setStatus("CANCELLED");
        orderRepository.save(order);
    }


    @Transactional
    public void markOrderAsPaid(Long orderId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (!order.getStatus().equals("CREATED")) {
            throw new RuntimeException("Only CREATED orders can be paid");
        }

        order.setStatus("PAID");
        orderRepository.save(order);
    }


    private OrderResponseDto mapToResponse(Order order) {

        List<OrderItemResponseDto> itemDtos =
                orderItemRepository.findByOrderId(order.getId())
                        .stream()
                        .map(item -> {
                            OrderItemResponseDto dto = new OrderItemResponseDto();
                            dto.setProductId(item.getProduct().getId());
                            dto.setProductName(item.getProduct().getName());
                            dto.setQuantity(item.getQuantity());
                            dto.setPrice(item.getPrice());
                            return dto;
                        })
                        .toList();

        OrderResponseDto dto = new OrderResponseDto();
        dto.setOrderId(order.getId());
        dto.setUserId(order.getUser().getId());
        dto.setUsername(order.getUser().getUsername());
        dto.setOrderDate(order.getOrderDate());
        dto.setStatus(order.getStatus());
        dto.setItems(itemDtos);

        return dto;
    }
}
