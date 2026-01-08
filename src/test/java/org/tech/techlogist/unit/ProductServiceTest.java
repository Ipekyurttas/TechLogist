package org.tech.techlogist.unit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.tech.techlogist.dto.product.ProductCreateRequestDto;
import org.tech.techlogist.dto.product.ProductResponseDto;
import org.tech.techlogist.dto.product.ProductUpdateRequestDto;
import org.tech.techlogist.entity.Category;
import org.tech.techlogist.entity.Product;
import org.tech.techlogist.entity.Stock;
import org.tech.techlogist.repository.CategoryRepository;
import org.tech.techlogist.repository.ProductRepository;
import org.tech.techlogist.repository.StockRepository;
import org.tech.techlogist.service.ProductService;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private StockRepository stockRepository;

    @InjectMocks
    private ProductService productService;

    private Product product;
    private Category category;
    private Stock stock;

    @BeforeEach
    void setUp() {
        category = new Category();
        category.setId(1L);
        category.setName("Elektronik");

        product = new Product();
        product.setId(10L);
        product.setName("Akıllı Telefon");
        product.setPrice(25000.0);
        product.setCategory(category);

        stock = new Stock();
        stock.setQuantity(50);
        stock.setMinQuantity(10);
        product.setStock(stock);
    }

    @Test
    void createProduct_SuccessfulScenario() {
        ProductCreateRequestDto dto = new ProductCreateRequestDto();
        dto.setName("Akıllı Telefon");
        dto.setCategoryId(1L);
        dto.setPrice(25000.0);
        dto.setStockQuantity(50);
        dto.setMinStockQuantity(10);
        when(productRepository.existsByName(dto.getName())).thenReturn(false);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(productRepository.save(any(Product.class))).thenReturn(product);
        ProductResponseDto result = productService.createProduct(dto);
        assertNotNull(result);
        assertEquals(product.getName(), result.getName());
        verify(stockRepository, times(1)).save(any(Stock.class));
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void createProduct_WhenProductAlreadyExists_ShouldThrowException() {
        ProductCreateRequestDto dto = new ProductCreateRequestDto();
        dto.setName("Mevcut Ürün");
        when(productRepository.existsByName(dto.getName())).thenReturn(true);
        assertThrows(RuntimeException.class, () -> productService.createProduct(dto));
        verify(productRepository, never()).save(any());
    }

    @Test
    void createProduct_WhenNegativeStock_ShouldThrowException() {
        ProductCreateRequestDto dto = new ProductCreateRequestDto();
        dto.setCategoryId(1L);
        dto.setStockQuantity(-5);
        dto.setMinStockQuantity(10);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        assertThrows(RuntimeException.class, () -> productService.createProduct(dto));
    }

    @Test
    void getProductById_SuccessfulScenario() {
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        ProductResponseDto result = productService.getProductById(10L);
        assertNotNull(result);
        assertEquals(10L, result.getId());
        assertEquals("Elektronik", result.getCategoryName());
    }

    @Test
    void getAllProducts_ShouldReturnList() {
        when(productRepository.findAll()).thenReturn(List.of(product));
        List<ProductResponseDto> result = productService.getAllProducts();
        assertEquals(1, result.size());
        verify(productRepository).findAll();
    }

    @Test
    void updateProduct_SuccessfulScenario() {
        ProductUpdateRequestDto updateDto = new ProductUpdateRequestDto();
        updateDto.setName("Yeni Ad");
        updateDto.setPrice(30000.0);
        updateDto.setStockQuantity(100);
        updateDto.setMinStockQuantity(20);
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(stockRepository.findByProductId(10L)).thenReturn(Optional.of(stock));
        when(productRepository.save(any(Product.class))).thenReturn(product);
        ProductResponseDto result = productService.updateProduct(10L, updateDto);
        assertNotNull(result);
        verify(stockRepository).save(any(Stock.class));
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void deleteProduct_SuccessfulScenario() {
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        productService.deleteProduct(10L);
        verify(productRepository).delete(product);
    }

    @Test
    void deleteProduct_WhenNotFound_ShouldThrowException() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> productService.deleteProduct(99L));
    }
}
