package com.service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/*
 * Encapsulates region-specific localized or municipal operational taxes.
 * 
 * Primary Use Case: Single-tier local taxation policies (eg, US State/County sales tax).
 * 
 * Fallback Default: 8.25%( 0.0825)
 */
public class RegionalTieredTaxStrategy implements TaxCalculationStrategy {
  private final BigDecimal regionalRate;

  public RegionalTieredTaxStrategy(BigDecimal regionalRate) {
    this.regionalRate = regionalRate != null ? regionalRate : new BigDecimal("0.0825"); // 8.25%
                                                                                        // baseline
                                                                                        // fallback
  }

  @Override
  public BigDecimal calculateTax(BigDecimal subtotal) {
    if (subtotal == null || subtotal.compareTo(BigDecimal.ZERO) <= 0) {
      return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    }
    return subtotal.multiply(regionalRate).setScale(2, RoundingMode.HALF_UP);
  }
}
