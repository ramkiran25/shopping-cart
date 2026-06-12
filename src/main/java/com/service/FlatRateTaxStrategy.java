package com.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class FlatRateTaxStrategy implements TaxCalculationStrategy {

    @Value("${tax.flat-rate}")
    private BigDecimal taxRate;

    @Override
    public BigDecimal calculateTax(BigDecimal subtotal) {

        if (subtotal == null || subtotal.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }

        return subtotal.multiply(taxRate)
                .setScale(2, RoundingMode.HALF_UP);
    }
}
