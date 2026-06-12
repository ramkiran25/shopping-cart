package com;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
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
  @DisplayName("Should successfully add items and compile precise cart totals")
  void shouldCalculateCorrectTotalsForValidCart() {
    // Arrange
    String product1 = "cheerios";
    String product2 = "cornflakes";

    when(priceApiGateway.fetchPrice(product1)).thenReturn(Optional.of(new BigDecimal("2.52")));
    when(priceApiGateway.fetchPrice(product2)).thenReturn(Optional.of(new BigDecimal("2.52")));

    // Subtotal = (2.52 * 2) + (2.52 * 1) = 5.04 + 2.52 = 7.56
    BigDecimal expectedSubtotal = new BigDecimal("7.56");
    BigDecimal mockTax = new BigDecimal("0.95"); // Mocked response from tax engine

    when(taxStrategy.calculateTax(expectedSubtotal)).thenReturn(mockTax);

    // Act
    shoppingCart.addProduct(product1, 2);
    shoppingCart.addProduct(product2, 1);
    CartState state = shoppingCart.getState();

    // Assert
    assertNotNull(state);
    assertEquals(2, state.items().size());
    assertEquals(expectedSubtotal, state.subtotal());
    assertEquals(mockTax, state.tax());
    assertEquals(new BigDecimal("8.51"), state.total()); // 7.56 + 0.95

    verify(priceApiGateway, times(1)).fetchPrice(product1);
    verify(priceApiGateway, times(1)).fetchPrice(product2);
    verify(taxStrategy, times(1)).calculateTax(expectedSubtotal);
  }

  @Test
  @DisplayName("Should normalize input product names to lowercase and trim whitespace")
  void shouldNormalizeProductNamesWhenAdded() {
    // Arrange
    when(priceApiGateway.fetchPrice("cheerios")).thenReturn(Optional.of(new BigDecimal("2.52")));
    when(taxStrategy.calculateTax(any(BigDecimal.class))).thenReturn(BigDecimal.ZERO.setScale(2));

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
  @DisplayName("Should throw IllegalStateException when price gateway lookup fails")
  void shouldThrowExceptionWhenPriceNotFound() {
    // Arrange
    when(priceApiGateway.fetchPrice("unknown")).thenReturn(Optional.empty());
    shoppingCart.addProduct("unknown", 1);

    // Act & Assert
    IllegalStateException exception =
        assertThrows(IllegalStateException.class, () -> shoppingCart.getState());
    assertTrue(exception.getMessage().contains("Price lookup failed for product: unknown"));
  }
}
