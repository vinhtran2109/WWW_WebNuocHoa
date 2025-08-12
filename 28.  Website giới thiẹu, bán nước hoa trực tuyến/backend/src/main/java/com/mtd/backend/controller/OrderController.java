package com.mtd.backend.controller;

import com.mtd.backend.dto.CheckoutDtos;
import com.mtd.backend.entity.Order;
import com.mtd.backend.entity.OrderItem;
import com.mtd.backend.entity.Product;
import com.mtd.backend.repository.OrderRepository;
import com.mtd.backend.repository.ProductRepository;
import com.mtd.backend.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final AuthService authService;

    public OrderController(ProductRepository productRepository, OrderRepository orderRepository, AuthService authService) {
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.authService = authService;
    }

    @PostMapping("/checkout")
    public ResponseEntity<?> checkout(@Valid @RequestBody CheckoutDtos.CheckoutRequest req,
                                      @RequestHeader(name = "Authorization", required = false) String bearer) {
        var user = authService.getUserFromToken(bearer);
        if (user == null) return ResponseEntity.status(401).body("Unauthorized");
        if (req.items == null || req.items.isEmpty()) return ResponseEntity.badRequest().body("Cart empty");

        var productMap = new HashMap<Long, Product>();
        int total = 0;
        for (var it : req.items) {
            var product = productRepository.findById(it.productId).orElseThrow();
            productMap.putIfAbsent(product.getId(), product);
            total += product.getPriceCents() * it.quantity;
        }
        var order = new Order();
        order.setUser(user);
        order.setTotalCents(total);
        order = orderRepository.save(order);

        for (var it : req.items) {
            var product = productMap.get(it.productId);
            var item = new OrderItem();
            item.setOrder(order);
            item.setProduct(product);
            item.setQuantity(it.quantity);
            item.setUnitPrice(product.getPriceCents());
            order.getItems().add(item);
        }
        order = orderRepository.save(order);

        var resp = new CheckoutDtos.OrderResponse();
        resp.orderId = order.getId();
        resp.totalCents = total;
        return ResponseEntity.ok(resp);
    }
}
