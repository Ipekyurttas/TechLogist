package org.tech.techlogist.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.tech.techlogist.dto.stock.StockCreateRequestDto;
import org.tech.techlogist.dto.stock.StockResponseDto;
import org.tech.techlogist.service.StockService;

@RestController
@RequestMapping("/api/stocks")
@RequiredArgsConstructor
public class StockController {

    private final StockService stockService;


    @PostMapping
    public ResponseEntity<StockResponseDto> createStock(
            @RequestBody StockCreateRequestDto dto) {

        StockResponseDto createdStock = stockService.createStock(dto);
        return new ResponseEntity<>(createdStock, HttpStatus.CREATED);
    }


    @GetMapping("/product/{productId}")
    public ResponseEntity<StockResponseDto> getStockByProductId(
            @PathVariable Long productId) {

        return ResponseEntity.ok(stockService.getStockByProductId(productId));
    }


    @PutMapping("/product/{productId}/increase")
    public ResponseEntity<StockResponseDto> increaseStock(
            @PathVariable Long productId,
            @RequestParam int quantity) {

        return ResponseEntity.ok(
                stockService.increaseStock(productId, quantity)
        );
    }


    @PutMapping("/product/{productId}/decrease")
    public ResponseEntity<StockResponseDto> decreaseStock(
            @PathVariable Long productId,
            @RequestParam int quantity) {

        return ResponseEntity.ok(
                stockService.decreaseStock(productId, quantity)
        );
    }
}
