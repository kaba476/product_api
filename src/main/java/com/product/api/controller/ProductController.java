package com.product.api.controller;

import com.product.core.model.Product;
import com.product.core.service.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    public ResponseEntity<Product> addProduct(@RequestBody Product product) {
        Product saved = productService.addProduct(product);
        return ResponseEntity.ok(saved);
    }

    @GetMapping
    public ResponseEntity<List<Product>> listAllProducts() {
        List<Product> products = productService.listAllProducts();
        return ResponseEntity.ok(products);
    }

    @PutMapping("/{id}/quantity")
    public ResponseEntity<Product> updateQuantity(@PathVariable Long id, @RequestParam int quantity) {
        Product updated = productService.updateQuantity(id, quantity);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/low-stock")
    public ResponseEntity<Long> countLowStock() {
        long count = productService.countLowStockProducts();
        return ResponseEntity.ok(count);
    }
}
