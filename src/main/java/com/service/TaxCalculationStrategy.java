package com.service;

import java.math.BigDecimal;

public interface TaxCalculationStrategy {
  BigDecimal calculateTax(BigDecimal subtotal);
}
