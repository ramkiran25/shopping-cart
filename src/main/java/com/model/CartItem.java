package com.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

public record CartItem(String productName, int quantity, BigDecimal pricePerUnit) {
  public BigDecimal getTotalPrice() {
    // Enforce monetary scale limits immediately on item calculation boundaries
    return pricePerUnit.multiply(BigDecimal.valueOf(quantity)).setScale(2, RoundingMode.HALF_UP);
  }
}
