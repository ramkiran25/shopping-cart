package com.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Set;
import com.model.CartItem;

/*
 * A multi-tiered strategy to handle advanced, itemized consumer taxation rules where specific
 * designed product categories carry reduced food-safety tax liabilities.
 * 
 * Primary Use Case: European Union VAT frameworks.
 * 
 * Fallback Defaults: * Standard Rate: 23%( 0.23)
 * 
 * Reduced Food Rate: 5%( 0.05)
 * 
 * Reduced Whitelist: "cornflakes", "weetabix","shreddies"
 * 
 * Extended Interface Methods: * calculateTax(subtotal): Evaluates a generic baseline fallback
 * calculation.
 * 
 * calculateItemizedTax(List<CartItem> items): Iterates through distinct item records to inspect
 * variations in product names, calculating itemized precision tax lines dynamically.
 */
public class VatTaxStrategy implements TaxCalculationStrategy {

  private final BigDecimal standardVat;
  private final BigDecimal reducedVat;
  private final Set<String> reducedVatProducts;

  public VatTaxStrategy(BigDecimal standardVat, BigDecimal reducedVat,
      Set<String> reducedVatProducts) {
    this.standardVat = standardVat != null ? standardVat : new BigDecimal("0.23"); // 23% Standard
                                                                                   // Polish VAT
    this.reducedVat = reducedVat != null ? reducedVat : new BigDecimal("0.05"); // 5% Reduced Basic
                                                                                // Food Items
    this.reducedVatProducts = reducedVatProducts != null ? reducedVatProducts
        : Set.of("cornflakes", "weetabix", "shreddies");
  }

  @Override
  public BigDecimal calculateTax(BigDecimal subtotal) {
    return subtotal.multiply(standardVat).setScale(2, RoundingMode.HALF_UP);
  }

  public BigDecimal calculateItemizedTax(List<CartItem> items) {
    BigDecimal totalTax = BigDecimal.ZERO;

    for (CartItem item : items) {
      BigDecimal rate =
          reducedVatProducts.contains(item.productName().toLowerCase().trim()) ? reducedVat
              : standardVat;

      totalTax = totalTax.add(item.getTotalPrice().multiply(rate));
    }

    return totalTax.setScale(2, RoundingMode.HALF_UP);
  }
}
