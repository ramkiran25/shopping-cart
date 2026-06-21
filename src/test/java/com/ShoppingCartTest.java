package com;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.model.CartState;
import com.service.ShoppingCart;

public class ShoppingCartTest {

  private ShoppingCart shoppingCart;

  @BeforeEach
  void setUp() {
    shoppingCart = new ShoppingCart();
  }

  @Test
  @DisplayName("Should successfully add a normalized product name and compute correct math state")
  void shouldNormalizeAndAddProduct() {
    // Arrange
    shoppingCart.addProduct("  ChEeRiOs  ", 2);
    Map<String, BigDecimal> priceMap = Map.of("cheerios", new BigDecimal("2.52"));

    // Act
    CartState state = shoppingCart.calculateState(priceMap);

    // Assert
    assertEquals("cheerios", state.items().get(0).productName());
    assertEquals(2, state.items().get(0).quantity());
    assertEquals(new BigDecimal("5.04"), state.subtotal());
    assertEquals(new BigDecimal("0.63"), state.tax()); // 12.5% of 5.04 rounded HALF_UP
    assertEquals(new BigDecimal("5.67"), state.total());
  }

  @Test
  @DisplayName("Should throw IllegalArgumentException when adding a blank product name")
  void shouldThrowExceptionForBlankProductName() {
    IllegalArgumentException exception =
        assertThrows(IllegalArgumentException.class, () -> shoppingCart.addProduct("   ", 5));
    assertEquals("Product name cannot be empty.", exception.getMessage());
  }

  @Test
  @DisplayName("Should throw IllegalArgumentException when quantity is zero or negative")
  void shouldThrowExceptionForInvalidQuantity() {
    assertThrows(IllegalArgumentException.class, () -> shoppingCart.addProduct("frosties", 0));
    assertThrows(IllegalArgumentException.class, () -> shoppingCart.addProduct("frosties", -1));
  }

  @Test
  @DisplayName("Should throw IllegalStateException when a required price definition is missing from the hydrator map")
  void shouldThrowExceptionWhenPriceNotFound() {
    // Arrange
    shoppingCart.addProduct("unknown", 1);
    Map<String, BigDecimal> emptyPriceMap = Map.of();

    // Act & Assert
    IllegalStateException exception =
        assertThrows(IllegalStateException.class, () -> shoppingCart.calculateState(emptyPriceMap));

    assertTrue(exception.getMessage().contains("Missing price definition for product: unknown"));
  }

  @Test
  @DisplayName("Boundary Math Condition — Should enforce strict HALF_UP rounding rules on complex decimals")
  void shouldEnforceStrictHalfUpRoundingOnComplexDecimals() {
    // Arrange: Pass an uneven fractional price ($2.525 which should round up to $2.53 per unit)
    shoppingCart.addProduct("cornflakes", 1);
    Map<String, BigDecimal> priceMap = Map.of("cornflakes", new BigDecimal("2.525"));

    // Act
    CartState state = shoppingCart.calculateState(priceMap);

    // Assert: Verify value rounds exactly up to 2.53 for a single unit subtotal
    assertEquals(new BigDecimal("2.53"), state.subtotal());
    assertEquals(new BigDecimal("0.32"), state.tax()); // 12.5% of 2.53 = 0.31625 -> 0.32
    assertEquals(new BigDecimal("2.85"), state.total()); // 2.53 + 0.32 = 2.85
  }
}
