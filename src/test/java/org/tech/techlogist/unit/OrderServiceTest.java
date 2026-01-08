package org.tech.techlogist.unit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.tech.techlogist.dto.order.*;
import org.tech.techlogist.entity.Order;
import org.tech.techlogist.entity.OrderItem;
import org.tech.techlogist.entity.Product;
import org.tech.techlogist.entity.User;
import org.tech.techlogist.repository.*;
import org.tech.techlogist.service.OrderService;
import org.tech.techlogist.service.StockService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private OrderItemRepository orderItemRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private StockService stockService;

    @InjectMocks
    private OrderService orderService;

    private User user;
    private Product product;
    private Order order;
    private OrderItem orderItem;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("testuser");

        product = new Product();
        product.setId(1L);
        product.setName("Laptop");
        product.setPrice(15000.0);

        order = new Order();
        order.setId(1L);
        order.setUser(user);
        order.setStatus("CREATED");
        order.setOrderDate(LocalDateTime.now());

        orderItem = new OrderItem();
        orderItem.setOrder(order);
        orderItem.setProduct(product);
        orderItem.setQuantity(1);
        orderItem.setPrice(15000.0);
    }

    @Test
    void createOrder_SuccessfulScenario() {
        OrderCreateRequestDto requestDto = new OrderCreateRequestDto();
        requestDto.setUserId(1L);
        OrderItemRequestDto itemRequest = new OrderItemRequestDto();
        itemRequest.setProductId(1L);
        itemRequest.setQuantity(2);
        requestDto.setItems(List.of(itemRequest));

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(orderItemRepository.findByOrderId(any())).thenReturn(List.of(orderItem));
        OrderResponseDto response = orderService.createOrder(requestDto);
        assertNotNull(response);
        assertEquals("CREATED", response.getStatus());
        verify(stockService).decreaseStock(1L, 2);
        verify(orderRepository).save(any(Order.class));
        verify(orderItemRepository).save(any(OrderItem.class));
    }

    @Test
    void createOrder_EmptyItems_ShouldThrowException() {
        OrderCreateRequestDto requestDto = new OrderCreateRequestDto();
        requestDto.setUserId(1L);
        requestDto.setItems(new ArrayList<>());
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        assertThrows(RuntimeException.class, () -> orderService.createOrder(requestDto), "Order must contain at least one product");
    }

    @Test
    void cancelOrder_SuccessfulScenario() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderItemRepository.findByOrderId(1L)).thenReturn(List.of(orderItem));
        orderService.cancelOrder(1L);
        assertEquals("CANCELLED", order.getStatus());
        verify(stockService).increaseStock(1L, 1);
        verify(orderRepository).save(order);
    }

    @Test
    void cancelOrder_WhenStatusNotCreated_ShouldThrowException() {
        order.setStatus("PAID");
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        assertThrows(RuntimeException.class, () -> orderService.cancelOrder(1L), "Only CREATED orders can be cancelled");
    }

    @Test
    void markOrderAsPaid_SuccessfulScenario() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        orderService.markOrderAsPaid(1L);
        assertEquals("PAID", order.getStatus());
        verify(orderRepository).save(order);
    }

    @Test
    void getOrdersByUser_ShouldReturnList() {
        when(orderRepository.findByUserId(1L)).thenReturn(List.of(order));
        when(orderItemRepository.findByOrderId(1L)).thenReturn(List.of(orderItem));
        List<OrderResponseDto> result = orderService.getOrdersByUser(1L);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals("testuser", result.get(0).getUsername());
    }
}
