package org.tech.techlogist.unit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.tech.techlogist.dto.stock.StockCreateRequestDto;
import org.tech.techlogist.dto.stock.StockResponseDto;
import org.tech.techlogist.entity.Product;
import org.tech.techlogist.entity.Stock;
import org.tech.techlogist.repository.ProductRepository;
import org.tech.techlogist.repository.StockRepository;
import org.tech.techlogist.service.StockService;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StockServiceTest {

    @Mock
    private StockRepository stockRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private StockService stockService;

    private Product product;
    private Stock stock;

    @BeforeEach
    void setUp() {
        product = new Product();
        product.setId(1L);
        product.setName("Test Laptop");

        stock = new Stock();
        stock.setId(10L);
        stock.setProduct(product);
        stock.setQuantity(50);
        stock.setMinQuantity(10);
    }

    @Test
    void createStock_SuccessfulScenario() {
        StockCreateRequestDto dto = new StockCreateRequestDto();
        dto.setProductId(1L);
        dto.setQuantity(50);
        dto.setMinQuantity(10);

        when(stockRepository.findByProductId(1L)).thenReturn(Optional.empty());
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(stockRepository.save(any(Stock.class))).thenReturn(stock);
        StockResponseDto result = stockService.createStock(dto);
        assertNotNull(result);
        assertEquals(50, result.getQuantity());
        verify(stockRepository).save(any(Stock.class));
    }

    @Test
    void createStock_WhenStockAlreadyExists_ShouldThrowException() {
        StockCreateRequestDto dto = new StockCreateRequestDto();
        dto.setProductId(1L);
        when(stockRepository.findByProductId(1L)).thenReturn(Optional.of(stock));
        assertThrows(RuntimeException.class, () -> stockService.createStock(dto));
    }

    @Test
    void increaseStock_SuccessfulScenario() {
        when(stockRepository.findByProductId(1L)).thenReturn(Optional.of(stock));
        when(stockRepository.save(any(Stock.class))).thenReturn(stock);
        StockResponseDto result = stockService.increaseStock(1L, 20);
        assertEquals(70, stock.getQuantity()); // 50 + 20
        verify(stockRepository).save(stock);
    }

    @Test
    void decreaseStock_SuccessfulScenario() {
        // Arrange
        when(stockRepository.findByProductId(1L)).thenReturn(Optional.of(stock));
        when(stockRepository.save(any(Stock.class))).thenReturn(stock);
        StockResponseDto result = stockService.decreaseStock(1L, 30);
        assertEquals(20, stock.getQuantity()); // 50 - 30
        verify(stockRepository).save(stock);
    }

    @Test
    void decreaseStock_WhenInsufficient_ShouldThrowException() {
        when(stockRepository.findByProductId(1L)).thenReturn(Optional.of(stock));
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> stockService.decreaseStock(1L, 100));
        assertEquals("Insufficient stock", exception.getMessage());
    }

    @Test
    void decreaseStock_BelowMinimum_ShouldPrintWarning() {
        // Arrange
        stock.setQuantity(15);
        stock.setMinQuantity(10);
        when(stockRepository.findByProductId(1L)).thenReturn(Optional.of(stock));
        when(stockRepository.save(any(Stock.class))).thenReturn(stock);
        stockService.decreaseStock(1L, 10); // 15 - 10 = 5 (Minimum 10'un altında)
        assertEquals(5, stock.getQuantity());
        verify(stockRepository).save(stock);
    }

    @Test
    void getStockByProductId_WhenNotFound_ShouldThrowException() {
        when(stockRepository.findByProductId(1L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> stockService.getStockByProductId(1L));
    }
}