package org.tech.techlogist.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.tech.techlogist.dto.product.ProductCreateRequestDto;
import org.tech.techlogist.dto.product.ProductUpdateRequestDto;
import org.tech.techlogist.dto.product.ProductResponseDto;
import org.tech.techlogist.entity.Category;
import org.tech.techlogist.entity.Product;
import org.tech.techlogist.entity.Stock;
import org.tech.techlogist.repository.CategoryRepository;
import org.tech.techlogist.repository.ProductRepository;
import org.tech.techlogist.repository.StockRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final StockRepository stockRepository;


    @Transactional
    public ProductResponseDto createProduct(ProductCreateRequestDto dto) {

        if (productRepository.existsByName(dto.getName())) {
            throw new RuntimeException("Product already exists");
        }

        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));

        Product product = new Product();
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setCategory(category);

        Product savedProduct = productRepository.save(product);

        if (dto.getStockQuantity() != null && dto.getMinStockQuantity() != null) {

            if (dto.getStockQuantity() < 0 || dto.getMinStockQuantity() < 0) {
                throw new RuntimeException("Stock quantity cannot be negative");
            }

            Stock stock = new Stock();
            stock.setProduct(savedProduct);
            stock.setQuantity(dto.getStockQuantity());
            stock.setMinQuantity(dto.getMinStockQuantity());

            stockRepository.save(stock);
        }

        return mapToResponse(savedProduct);
    }


    public List<ProductResponseDto> getAllProducts() {
        return productRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public ProductResponseDto getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        return mapToResponse(product);
    }


    @Transactional
    public ProductResponseDto updateProduct(Long id, ProductUpdateRequestDto dto) {

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if (!product.getName().equals(dto.getName())
                && productRepository.existsByName(dto.getName())) {
            throw new RuntimeException("Product name already exists");
        }

        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());

        if (dto.getCategoryId() != null) {
            Category category = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found"));
            product.setCategory(category);
        }

        if (dto.getStockQuantity() != null && dto.getMinStockQuantity() != null) {

            if (dto.getStockQuantity() < 0 || dto.getMinStockQuantity() < 0) {
                throw new RuntimeException("Stock quantity cannot be negative");
            }

            Stock stock = stockRepository.findByProductId(product.getId())
                    .orElse(new Stock());

            stock.setProduct(product);
            stock.setQuantity(dto.getStockQuantity());
            stock.setMinQuantity(dto.getMinStockQuantity());

            stockRepository.save(stock);
        }

        return mapToResponse(productRepository.save(product));
    }


    @Transactional
    public void deleteProduct(Long id) {

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        productRepository.delete(product);
    }

    private ProductResponseDto mapToResponse(Product product) {

        ProductResponseDto dto = new ProductResponseDto();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());

        if (product.getCategory() != null) {
            dto.setCategoryId(product.getCategory().getId());
            dto.setCategoryName(product.getCategory().getName());
        }

        if (product.getStock() != null) {
            dto.setStockQuantity(product.getStock().getQuantity());
            dto.setMinStockQuantity(product.getStock().getMinQuantity());
        }

        return dto;
    }
}
