# Stateful Shopping Cart Subsystem

A stateful shopping cart implementation built using **Java 17** and **Spring Boot**.

The application allows products to be added to a shopping cart, retrieves product pricing from the provided Price API, and calculates the cart subtotal, tax, and total using precise monetary calculations based on `BigDecimal`.

---

## Features

- Add products to the cart by product name and quantity
- Retrieve product prices from the external Price API
- Maintain cart state across multiple additions
- Calculate:
  - Subtotal
  - Tax (12.5%)
  - Total payable
- Monetary calculations performed using `BigDecimal`
- Values rounded to two decimal places using `RoundingMode.HALF_UP`
- Comprehensive unit test coverage

---

## Design Overview

The solution separates business logic from external integrations to keep the codebase simple, testable, and maintainable.

### Components

#### ShoppingCart

Responsible for:

- Managing cart contents
- Aggregating product quantities
- Calculating the current cart state

#### PriceApiGateway

Abstraction over the external Price API.

**Benefits:**

- Supports mocking during unit testing
- Isolates HTTP communication concerns
- Allows the underlying implementation to be replaced without affecting business logic

#### TaxCalculationStrategy

Encapsulates tax calculation logic.

The current implementation applies the required fixed tax rate of **12.5%** while keeping the calculation logic isolated and easily testable.

#### CartItem and CartState

Implemented as Java Records to provide immutable representations of:

- Individual cart items
- Calculated cart totals

---

## Design Decisions

### BigDecimal for Monetary Calculations

All monetary values are represented using `BigDecimal` to avoid floating-point precision issues and ensure accurate financial calculations.

### Dependency Inversion

The shopping cart depends on the `PriceApiGateway` abstraction rather than a concrete HTTP client implementation. This improves testability and separation of concerns.

### Immutable State

Cart results are exposed through immutable record types, preventing accidental modification of calculated values.

---

## Testing

The solution includes unit tests covering:

- Product addition
- Quantity aggregation
- Subtotal calculation
- Tax calculation
- Total calculation
- Rounding behavior
- Price API integration using mocked responses

---

## Running Tests

Execute all tests using:

```bash
mvn test
```

---

## Assumptions

- Product names are treated as case-insensitive.
- Product prices are retrieved from the external Price API.
- Tax is calculated at a fixed rate of **12.5%** as specified in the requirements.
- Monetary values are rounded to two decimal places using `RoundingMode.HALF_UP`.

---
