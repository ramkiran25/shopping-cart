package com.api;

import java.math.BigDecimal;
import java.util.Optional;

/*
 * The subsystem handles external pricing data using the Gateway Design Pattern . By encapsulating
 * raw HTTP execution, serialization semantics, network resilience properties, and offline fallback
 * fallbacks behind a crisp PriceApiGatewayabstraction, the domain layer remains perfectly isolated
 * from infrastructural dependencies ( Separation of Concerns ).
 * 
 * This decoupling ensures that the core ShoppingCartdomain remains entirely unaware of whether
 * prices are fetched from a remote REST API, an in-memory database, or a static testing double.
 */
public interface PriceApiGateway {
  Optional<BigDecimal> fetchPrice(String productName);
}
