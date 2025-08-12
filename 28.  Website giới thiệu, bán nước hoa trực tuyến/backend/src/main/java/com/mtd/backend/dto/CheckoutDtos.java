package com.mtd.backend.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public class CheckoutDtos {
    public static class CheckoutItem {
        @NotNull public Long productId;
        @Min(1) public Integer quantity;
    }
    public static class CheckoutRequest {
        public List<CheckoutItem> items;
    }
    public static class OrderResponse {
        public Long orderId;
        public Integer totalCents;
    }
}
