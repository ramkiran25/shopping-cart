package com.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import org.springframework.stereotype.Component;

@Component("taxExemptStrategy")
public class TaxExemptStrategy implements TaxCalculationStrategy {
  @Override
  public BigDecimal calculateTax(BigDecimal subtotal) {
    return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
  }
}
