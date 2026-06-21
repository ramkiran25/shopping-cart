package com.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import com.model.CartItem;
import com.model.CartState;

public class ShoppingCart {

    private static final BigDecimal TAX_RATE = new BigDecimal("0.125");
    private final Map<String, Integer> itemQuantities = new LinkedHashMap<>();

    public synchronized void addProduct(String productName, int quantity) {
        if (productName == null || productName.isBlank()) {
            throw new IllegalArgumentException("Product name cannot be empty.");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero.");
        }

        String normalizedName = productName.trim().toLowerCase();
        itemQuantities.put(normalizedName, itemQuantities.getOrDefault(normalizedName, 0) + quantity);
    }

    public synchronized CartState calculateState(Map<String, BigDecimal> productPrices) {
        List<CartItem> itemsList = itemQuantities.entrySet().stream()
            .map(entry -> {
                String name = entry.getKey();
                int quantity = entry.getValue();
                BigDecimal price = productPrices.get(name);
                
                if (price == null) {
                    throw new IllegalStateException("Missing price definition for product: " + name);
                }
                return new CartItem(name, quantity, price);
            }).toList();

        BigDecimal subtotal = itemsList.stream()
            .map(CartItem::getTotalPrice)
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .setScale(2, RoundingMode.HALF_UP);

        BigDecimal tax = subtotal.multiply(TAX_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = subtotal.add(tax).setScale(2, RoundingMode.HALF_UP);

        return new CartState(itemsList, subtotal, tax, total);
    }

    public synchronized List<String> getProductNames() {
        return List.copyOf(itemQuantities.keySet());
    }
}