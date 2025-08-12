package com.mtd.backend.controller;

import com.mtd.backend.entity.Product;
import com.mtd.backend.repository.CategoryRepository;
import com.mtd.backend.repository.ProductRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public ProductController(ProductRepository productRepository, CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    @GetMapping
    public ResponseEntity<List<Product>> list(@RequestParam(value = "q", required = false) String q,
                                              @RequestParam(value = "category", required = false) String categorySlug) {
        List<Product> result;
        if (q != null && !q.isBlank()) {
            result = productRepository.findByNameContainingIgnoreCase(q);
        } else {
            result = productRepository.findAll();
        }
        // Note: category filter can be added by custom query; keeping minimal for now.
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{slug}")
    public ResponseEntity<?> detail(@PathVariable String slug) {
        return productRepository.findBySlug(slug)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
