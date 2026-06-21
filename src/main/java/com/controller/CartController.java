package com.controller;

import com.api.PriceApiGateway;
import com.model.CartState;
import com.service.ShoppingCart;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
    public ResponseEntity<String> addProduct(
            @PathVariable String cartId,
            @RequestParam String productName,
            @RequestParam int quantity) {
        
        ShoppingCart cart = sessionCarts.computeIfAbsent(cartId, id -> new ShoppingCart());
        
        try {
            cart.addProduct(productName, quantity);
            return ResponseEntity.ok(String.format("Successfully added %d x '%s' to cart: %s", quantity, productName, cartId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{cartId}")
    @Operation(summary = "Retrieve compiled cart summary")
    public ResponseEntity<?> getCartState(@PathVariable String cartId) {
        ShoppingCart cart = sessionCarts.get(cartId);
        if (cart == null) {
            return ResponseEntity.notFound().build();
        }
        
        try {
            Map<String, BigDecimal> priceMap = new HashMap<>();
            for (String productName : cart.getProductNames()) {
                BigDecimal price = priceApiGateway.fetchPrice(productName)
                        .orElseThrow(() -> new IllegalStateException("Price payload was empty for product: " + productName));
                priceMap.put(productName, price);
            }

            CartState state = cart.calculateState(priceMap);
            return ResponseEntity.ok(state);
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