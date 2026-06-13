package com.controller;

import com.api.PriceApiGateway;
import com.model.CartState;
import com.service.FlatRateTaxStrategy;
import com.service.ShoppingCart;
import com.service.TaxCalculationStrategy;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.net.http.HttpClient;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/v1/cart")
@Tag(name = "Shopping Cart Subsystem", description = "REST API endpoints to interact with the stateful cart engine via Swagger")
public class CartController {

    // Simulating a database session lookup using an in-memory storage map
    private final Map<String, ShoppingCart> sessionCarts = new ConcurrentHashMap<>();
    private final PriceApiGateway priceApiGateway;
    private final TaxCalculationStrategy defaultTaxStrategy;

    public CartController() {
        // Wiring your pure vanilla Java components manually
        this.priceApiGateway = new com.api.HttpPriceApiGateway(HttpClient.newHttpClient(), "https://equalexperts.github.io");
        this.defaultTaxStrategy = new FlatRateTaxStrategy(new BigDecimal("0.125")); // 12.5% default flat tax
    }

    @PostMapping("/{cartId}/items")
    @Operation(summary = "Add a product to the cart", description = "Normalizes product inputs and lazily captures intent.")
    public ResponseEntity<String> addProduct(
            @PathVariable String cartId,
            @RequestParam String productName,
            @RequestParam int quantity) {
        
        ShoppingCart cart = sessionCarts.computeIfAbsent(cartId, 
            id -> new ShoppingCart(priceApiGateway, defaultTaxStrategy));
        
        try {
            cart.addProduct(productName, quantity);
            return ResponseEntity.ok(String.format("Successfully added %d x '%s' to cart: %s", quantity, productName, cartId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{cartId}")
    @Operation(summary = "Retrieve compiled cart summary", description = "Compiles the immutable snapshot state including pricing subtotals and rounded taxes.")
    public ResponseEntity<?> getCartState(@PathVariable String cartId) {
        ShoppingCart cart = sessionCarts.get(cartId);
        if (cart == null) {
            return ResponseEntity.notFound().build();
        }
        
        try {
            CartState state = cart.getState();
            return ResponseEntity.ok(state);
        } catch (IllegalStateException e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
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