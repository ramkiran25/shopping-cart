package com.config;

import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import com.api.PriceApiGateway;
import com.service.ShoppingCart;
import com.service.TaxCalculationStrategy;

@Configuration
public class ShoppingCartConfiguration {
  private final Map<String, TaxCalculationStrategy> taxStrategies;

  // Spring automatically injects all beans implementing TaxCalculationStrategy into this map
  public ShoppingCartConfiguration(Map<String, TaxCalculationStrategy> taxStrategies) {
      this.taxStrategies = taxStrategies;
  }

  @Bean
  @Scope("prototype") // Ensures a fresh stateful cart instance is given per user transaction/session
  public ShoppingCart shoppingCart(
          PriceApiGateway priceApiGateway, 
          @Value("${shopping.cart.tax-strategy:flatRateTaxStrategy}") String strategyName) {
      
      TaxCalculationStrategy selectedStrategy = taxStrategies.get(strategyName);
      
      if (selectedStrategy == null) {
          throw new IllegalArgumentException("Unknown tax strategy configuration: " + strategyName);
      }

      return new ShoppingCart(priceApiGateway, selectedStrategy);
  }
}
