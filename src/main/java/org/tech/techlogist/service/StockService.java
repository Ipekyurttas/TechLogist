package org.tech.techlogist.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.tech.techlogist.dto.stock.StockCreateRequestDto;
import org.tech.techlogist.dto.stock.StockResponseDto;
import org.tech.techlogist.entity.Product;
import org.tech.techlogist.entity.Stock;
import org.tech.techlogist.repository.ProductRepository;
import org.tech.techlogist.repository.StockRepository;

@Service
@RequiredArgsConstructor
public class StockService {

    private final StockRepository stockRepository;
    private final ProductRepository productRepository;


    @Transactional
    public StockResponseDto createStock(StockCreateRequestDto dto) {

        if (dto.getQuantity() < 0 || dto.getMinQuantity() < 0) {
            throw new RuntimeException("Stock values cannot be negative");
        }

        if (stockRepository.findByProductId(dto.getProductId()).isPresent()) {
            throw new RuntimeException("Stock already exists for this product");
        }

        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found"));

        Stock stock = new Stock();
        stock.setProduct(product);
        stock.setQuantity(dto.getQuantity());
        stock.setMinQuantity(dto.getMinQuantity());

        return mapToResponse(stockRepository.save(stock));
    }


    public StockResponseDto getStockByProductId(Long productId) {
        Stock stock = stockRepository.findByProductId(productId)
                .orElseThrow(() -> new RuntimeException("Stock not found for product"));
        return mapToResponse(stock);
    }


    @Transactional
    public StockResponseDto increaseStock(Long productId, int quantity) {

        if (quantity <= 0) {
            throw new RuntimeException("Increase quantity must be greater than zero");
        }

        Stock stock = stockRepository.findByProductId(productId)
                .orElseThrow(() -> new RuntimeException("Stock not found"));

        stock.setQuantity(stock.getQuantity() + quantity);
        return mapToResponse(stockRepository.save(stock));
    }


    @Transactional
    public StockResponseDto decreaseStock(Long productId, int quantity) {

        if (quantity <= 0) {
            throw new RuntimeException("Decrease quantity must be greater than zero");
        }

        Stock stock = stockRepository.findByProductId(productId)
                .orElseThrow(() -> new RuntimeException("Stock not found"));

        if (stock.getQuantity() < quantity) {
            throw new RuntimeException("Insufficient stock");
        }

        stock.setQuantity(stock.getQuantity() - quantity);

        if (stock.getQuantity() < stock.getMinQuantity()) {
            System.out.println(
                    "WARNING: Stock below minimum level for product " +
                            stock.getProduct().getName()
            );
        }

        return mapToResponse(stockRepository.save(stock));
    }


    private StockResponseDto mapToResponse(Stock stock) {

        StockResponseDto dto = new StockResponseDto();
        dto.setId(stock.getId());
        dto.setProductId(stock.getProduct().getId());
        dto.setProductName(stock.getProduct().getName());
        dto.setQuantity(stock.getQuantity());
        dto.setMinQuantity(stock.getMinQuantity());

        return dto;
    }
}
