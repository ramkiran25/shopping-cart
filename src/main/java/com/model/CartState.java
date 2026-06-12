package com.model;

import java.math.BigDecimal;
import java.util.List;

public record CartState(List<CartItem> items, BigDecimal subtotal, BigDecimal tax,
    BigDecimal total) {
}
