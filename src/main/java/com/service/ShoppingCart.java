package com.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import com.api.PriceApiGateway;
import com.model.CartItem;
import com.model.CartState;

/**
 * The core domain orchestrator representing a stateful shopping cart.
 */
public class ShoppingCart {
  /** Gateway facade for fetching live remote product pricing models. */
  private final PriceApiGateway priceApiGateway;
  /** Polymorphic strategy for evaluating downstream taxation liabilities. */
  private final TaxCalculationStrategy taxStrategy;
  private final Map<String, Integer> itemQuantities = new LinkedHashMap<>();

  public ShoppingCart(PriceApiGateway priceApiGateway, TaxCalculationStrategy taxStrategy) {
    this.priceApiGateway = priceApiGateway;
    this.taxStrategy = taxStrategy;
  }

  public void addProduct(String productName, int quantity) {
    if (productName == null || productName.isBlank()) {
      throw new IllegalArgumentException("Product name cannot be empty.");
    }
    if (quantity <= 0) {
      throw new IllegalArgumentException("Quantity must be greater than zero.");
    }

    String normalizedName = productName.trim().toLowerCase();
    itemQuantities.put(normalizedName, itemQuantities.getOrDefault(normalizedName, 0) + quantity);
  }

  public CartState getState() {
    // Pass 1: Hydrate item prices and map to CartItem domain models
    List<CartItem> itemsList = itemQuantities.entrySet().stream()
        .map(entry -> createCartItem(entry.getKey(), entry.getValue())).toList(); // Simplified Java
                                                                                  // 16+ syntax
                                                                                  // instead of
                                                                                  // Collectors.toList()

    // Pass 2: Sum up subtotal and enforce 2 decimal rounding immediately
    BigDecimal subtotal = itemsList.stream().map(CartItem::getTotalPrice)
        .reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2, RoundingMode.HALF_UP);

    // Pass 3: Calculate scaled tax and aggregate to total
    BigDecimal tax = taxStrategy.calculateTax(subtotal);
    BigDecimal total = subtotal.add(tax).setScale(2, RoundingMode.HALF_UP);

    return new CartState(itemsList, subtotal, tax, total);
  }

  // Private helper method to drastically clean up the stream readability
  private CartItem createCartItem(String name, int quantity) {
    BigDecimal price = priceApiGateway.fetchPrice(name)
        .orElseThrow(() -> new IllegalStateException("Price lookup failed for product: " + name));
    return new CartItem(name, quantity, price);
  }
}
