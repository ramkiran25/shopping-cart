package com.service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/*
 * Applies an immutable, predictable baseline percentage across the entire subtotal volume.
 * 
 * Primary Use Case: Default corporate or domestic tax rates.
 * 
 * Fallback Default: 12.5%( 0.125)
 * 
 * Constructor Injection:
 */
public class FlatRateTaxStrategy implements TaxCalculationStrategy {

  private final BigDecimal taxRate;

  // Constructor configuration injection
  public FlatRateTaxStrategy(BigDecimal taxRate) {
    this.taxRate = taxRate != null ? taxRate : new BigDecimal("0.125"); // 12.5% default fallback
  }

  @Override
  public BigDecimal calculateTax(BigDecimal subtotal) {
    if (subtotal == null || subtotal.compareTo(BigDecimal.ZERO) <= 0) {
      return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    }
    return subtotal.multiply(taxRate).setScale(2, RoundingMode.HALF_UP);
  }
}
