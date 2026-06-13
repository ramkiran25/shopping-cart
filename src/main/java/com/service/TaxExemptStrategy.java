package com.service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/*
 * A zero-tax calculation implementation pattern ( Null Object Pattern variation).
 * 
 * Primary Use Case: Business-to-Business (B2B) operational modes, tax holidays, or wholesale
 * transactions.
 * 
 * Behavior: Constantly returns 0.00
 */
public class TaxExemptStrategy implements TaxCalculationStrategy {
  @Override
  public BigDecimal calculateTax(BigDecimal subtotal) {
    return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
  }
}
