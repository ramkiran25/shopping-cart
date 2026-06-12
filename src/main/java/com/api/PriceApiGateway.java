package com.api;

import java.math.BigDecimal;
import java.util.Optional;

public interface PriceApiGateway {
  Optional<BigDecimal> fetchPrice(String productName);
}
