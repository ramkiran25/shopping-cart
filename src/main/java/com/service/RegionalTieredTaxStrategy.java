package com.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component("regionalTieredTaxStrategy")
public class RegionalTieredTaxStrategy implements TaxCalculationStrategy {
  private final BigDecimal regionalRate;

  // Dynamically injected based on active operational profile or properties
  public RegionalTieredTaxStrategy(@Value("${tax.rate.regional:0.0825}") String regionalRate) {
    this.regionalRate = new BigDecimal(regionalRate);
  }

  @Override
  public BigDecimal calculateTax(BigDecimal subtotal) {
    if (subtotal == null || subtotal.compareTo(BigDecimal.ZERO) <= 0) {
      return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    }
    return subtotal.multiply(regionalRate).setScale(2, RoundingMode.HALF_UP);
  }
}
