package com.unicasolid.shophub.model;

import java.math.BigDecimal;

public record OrderItem(String productId, String productName, ProductCategory category, BigDecimal unitPrice, int quantity) {
    public BigDecimal getTotalPrice() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}
