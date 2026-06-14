package com;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.api.PriceApiGateway;
import com.exception.InvalidProductException;
import com.model.CartState;
import com.service.ShoppingCart;
import com.service.TaxCalculationStrategy;

@ExtendWith(MockitoExtension.class)
public class ShoppingCartTest {

  @Mock
  private PriceApiGateway priceApiGateway;

  @Mock
  private TaxCalculationStrategy taxStrategy;

  private ShoppingCart shoppingCart;

  @BeforeEach
  void setUp() {
    shoppingCart = new ShoppingCart(priceApiGateway, taxStrategy);
  }

  @Test
  @DisplayName("Should successfully add a normalized product name and maintain state layout")
  void shouldNormalizeAndAddProduct() {
    // Arrange
    when(priceApiGateway.fetchPrice("cheerios")).thenReturn(Optional.of(new BigDecimal("2.52")));
    when(taxStrategy.calculateTax(any(BigDecimal.class))).thenReturn(BigDecimal.ZERO);

    // Act
    shoppingCart.addProduct("  ChEeRiOs  ", 2);
    CartState state = shoppingCart.getState();

    // Assert
    assertEquals("cheerios", state.items().get(0).productName());
    assertEquals(2, state.items().get(0).quantity());
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
  @DisplayName("Should throw InvalidProductException when price gateway lookup fails")
  void shouldThrowExceptionWhenPriceNotFound() {
    // Arrange
    when(priceApiGateway.fetchPrice("unknown")).thenReturn(Optional.empty());
    shoppingCart.addProduct("unknown", 1);

    // Act & Assert
    InvalidProductException exception =
        assertThrows(InvalidProductException.class, () -> shoppingCart.getState());

    // Fixed string check to align with the core domain message layout
    assertTrue(exception.getMessage().contains("Invalid or unrecognized product:unknown"));
  }

  @Test
  @DisplayName("Boundary Math Condition — Should enforce strict HALF_UP rounding rules on complex decimals")
  void shouldEnforceStrictHalfUpRoundingOnComplexDecimals() {
    // Arrange: Mock an uneven fractional price (e.g., $2.525 which should round up to $2.53)
    when(priceApiGateway.fetchPrice("cornflakes")).thenReturn(Optional.of(new BigDecimal("2.525")));
    // Mock tax calculation behavior to simply return 0.00 to isolate subtotal verification
    when(taxStrategy.calculateTax(any(BigDecimal.class))).thenReturn(new BigDecimal("0.00"));

    shoppingCart.addProduct("cornflakes", 1);

    // Act
    CartState state = shoppingCart.getState();

    // Assert: Verify value rounds exactly up to 2.53
    assertEquals(new BigDecimal("2.53"), state.subtotal());
    assertEquals(new BigDecimal("2.53"), state.total());
  }

  private BigDecimal any(Class<BigDecimal> type) {
    return org.mockito.ArgumentMatchers.any(type);
  }
}
