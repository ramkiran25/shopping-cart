package com;

import java.math.BigDecimal;
import java.net.http.HttpClient;
import com.api.HttpPriceApiGateway;
import com.api.PriceApiGateway;
import com.model.CartState;
import com.service.FlatRateTaxStrategy;
import com.service.ShoppingCart;
import com.service.TaxCalculationStrategy;

public class ShoppingCartApplication {

  public static void main(String[] args) {
    // 1. Setup pure dependencies
    HttpClient httpClient = HttpClient.newHttpClient();
    PriceApiGateway priceGateway =
        new HttpPriceApiGateway(httpClient, "https://equalexperts.github.io");
    TaxCalculationStrategy taxStrategy = new FlatRateTaxStrategy(new BigDecimal("0.125"));

    // 2. Inject contextually
    ShoppingCart cart = new ShoppingCart(priceGateway, taxStrategy);

    // 3. Process business intent
    cart.addProduct("cheerios", 2);
    cart.addProduct("cornflakes", 1);

    CartState finalState = cart.getState();

    System.out.println("Subtotal: " + finalState.subtotal());
    System.out.println("Tax:      " + finalState.tax());
    System.out.println("Total:    " + finalState.total());
  }

}
