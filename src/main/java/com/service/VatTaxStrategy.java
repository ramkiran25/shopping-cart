package com.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.model.CartItem;

@Component
public class VatTaxStrategy implements TaxCalculationStrategy {

  @Value("${vat.standard-rate}")
  private BigDecimal standardVat;

  @Value("${vat.reduced-rate}")
  private BigDecimal reducedVat;

  @Value("#{'${products.reduced-vat}'.split(',')}")
  private Set<String> reducedVatProducts;

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
