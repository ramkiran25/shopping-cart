package com.service;

import java.math.BigDecimal;

/*
 * The subsystem evaluates transactions utilizing the Strategy Design Pattern to calculate taxation
 * boundaries. By isolating financial calculation algorithms behind a unified interface, the engine
 * remains highly extensible, strictly testable, and completely decoupled from the core shopping
 * cart processing lifecycle.
 * 
 * Adding a new tax tier or regional compliance rule requires introducing a new class implementation
 * without altering existing domain execution logic ( Open/Closed Principle ).
 */
public interface TaxCalculationStrategy {
  BigDecimal calculateTax(BigDecimal subtotal);
}
