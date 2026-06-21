package com.controller;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.api.PriceApiGateway;
import com.model.CartState;
import com.service.ShoppingCart;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/cart")
@Tag(name = "Shopping Cart Subsystem")
public class CartController {

  private final Map<String, ShoppingCart> sessionCarts = new ConcurrentHashMap<>();
  private final PriceApiGateway priceApiGateway;

  public CartController(PriceApiGateway priceApiGateway) {
    this.priceApiGateway = priceApiGateway;
  }

  @PostMapping("/{cartId}/items")
  @Operation(summary = "Add a product to the cart")
  public ResponseEntity<String> addProduct(@PathVariable String cartId,
      @RequestParam String productName, @RequestParam int quantity) {

    ShoppingCart cart = sessionCarts.computeIfAbsent(cartId, id -> new ShoppingCart());

    try {
      cart.addProduct(productName, quantity);
      return ResponseEntity.ok(
          String.format("Successfully added %d x '%s' to cart: %s", quantity, productName, cartId));
    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().body(e.getMessage());
    }
  }

  @GetMapping("/{cartId}")
  @Operation(summary = "Retrieve compiled cart summary")
  public ResponseEntity<?> getCartState(@PathVariable String cartId) {
    ShoppingCart cart = sessionCarts.get(cartId);
    if (cart == null) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(Map.of("message", "Cart session not found: " + cartId));
    }

    try {
      Map<String, BigDecimal> priceMap = new HashMap<>();
      for (String productName : cart.getProductNames()) {
        // If the external gateway throws a ProductNotFoundException (404)
        // or an UpstreamDependencyException, catch it or handle the optional
        java.math.BigDecimal price = priceApiGateway.fetchPrice(productName)
            .orElseThrow(() -> new com.exception.ProductNotFoundException(
                "Price database definition completely empty for item: " + productName));

        priceMap.put(productName, price);
      }

      CartState state = cart.calculateState(priceMap);
      return ResponseEntity.ok(state);

    } catch (com.exception.ProductNotFoundException e) {
      // Return a clean 404 client alert if they added an item name that doesn't exist upstream
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", e.getMessage()));

    } catch (com.exception.UpstreamDependencyException e) {
      // Return a 502/504 Bad Gateway if Equal Experts' server drops out entirely
      return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(
          Map.of("message", "External pricing catalog service failed.", "details", e.getMessage()));

    } catch (IllegalArgumentException | IllegalStateException e) {
      return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
    }
  }

  @DeleteMapping("/{cartId}")
  @Operation(summary = "Clear/Reset a specific cart session")
  public ResponseEntity<String> clearCart(@PathVariable String cartId) {
    if (sessionCarts.remove(cartId) != null) {
      return ResponseEntity.ok("Cart session cleared successfully.");
    }
    return ResponseEntity.notFound().build();
  }
}
