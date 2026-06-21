# Stateful Shopping Cart Subsystem

A production-grade, stateful shopping cart backend platform built using **Java 17** and **Spring Boot 3.x**.

The architecture separates the core business domain model from side-effect-heavy external network infrastructure layer boundaries. The platform orchestrates real-time product data lookup via a remote REST Pricing API and executes precise financial accounting routines natively leveraging `BigDecimal` with strict rounding controls.

---

## Technical Enhancements & Architecture Pivot

Based on production-focused code evaluation parameters, the codebase underwent a major architectural refactoring to remove technical debt and over-engineered abstractions:

1. **Pure Domain Model Pattern:** The `ShoppingCart` was completely decoupled from network I/O. It no longer holds infrastructure dependencies or directly invokes remote HTTP calls inside state-generation loops. It functions as a pure mathematical and state-mutation entity that accepts pre-hydrated pricing definitions deterministically.
2. **YAGNI Alignment (Removal of Fluff):** Extraneous abstractions that added unrequested complexity—such as multi-tiered tax strategy interfaces (`TaxCalculationStrategy`, `VatTaxStrategy`, `TaxExemptStrategy`) and premature local caching layers (`@Cacheable`, `ProductManagementService`)—were stripped out to maintain a clean, maintainable, and highly targeted codebase.
3. **True Inversion of Control (IoC):** Manual object instantiation chains (`new HttpPriceApiGateway(...)`) inside controller layers were replaced with framework-managed Dependency Injection (DI) beans via standard constructor wiring.
4. **Resilient Network & Error Handling:** The manual regex-based JSON text extraction logic was completely removed in favor of standard, resilient Jackson data mapping. Upstream failure profiles are now categorized into explicit custom exception boundaries (`ProductNotFoundException` vs. `UpstreamDependencyException`) to match professional API standards.

---

## System Component Layout

### 1. Pure Domain Layer
* **`ShoppingCart`**: The core stateful business orchestrator. It manages item quantity aggregation, handles input normalization metrics, and computes mathematical outputs natively.
* **`CartItem` & `CartState`**: Immutable Java records representing thread-safe transactional snapshots and financial breakdowns.

### 2. External Integration Layer
* **`HttpPriceApiGateway`**: A robust HTTP gateway powered by Java’s native `HttpClient` and Spring's `ObjectMapper`. It safely communicates with the remote vendor catalog (`https://equalexperts.github.io`), handles response telemetry, and manages protocol serialization boundaries.

### 3. API Controller Layer
* **`CartController`**: A stateful REST API gateway providing fully documented, interactive endpoints. It acts as an orchestrator—handling client session state lookups, invoking the price gateway to fetch pricing definitions, and passing those definitions to the domain cart to compile the final state representation.

---

## REST API Operational Specifications

The system exposes documentation natively via **Springdoc OpenAPI 3.x**.

### Swagger UI Interactivity Dashboard
When running the application locally, view, inspect, and test the endpoints directly by navigating to:
👉 **`http://localhost:8080/swagger-ui/index.html`**

### Active Endpoint Routes

| HTTP Verb | Resource Path | Description | Input Parameters | Expected Status Codes |
| :--- | :--- | :--- | :--- | :--- |
| **POST** | `/api/v1/cart/{cartId}/items` | Appends a normalized product item to a specific session cart | `productName` (String), `quantity` (int) | `200 OK`, `400 Bad Request` |
| **GET** | `/api/v1/cart/{cartId}` | Resolves prices via the integration gateway and returns the aggregated financial balance state sheet | `{cartId}` (Path) | `200 OK`, `404 Not Found` |
| **DELETE** | `/api/v1/cart/{cartId}` | Explicitly flushes and purges the session cart instance cache entirely | `{cartId}` (Path) | `200 OK`, `404 Not Found` |

*Note: For testing data items against the live external sandbox environment, utilize valid case-sensitive product naming strings such as `cheerio`, `cornflakes`, `frosties`, `shreddies`, or `weetabix`.*

---

## Financial Accounting Parameters

- **Fixed Taxation Vector:** In absolute adherence to operational business rules, taxation metrics are evaluated at a fixed rate of **12.5%** (`0.125`).
- **Precision Scale Strategy:** All pricing vectors enforce a strict monetary resolution footprint of two decimal places, utilizing `RoundingMode.HALF_UP` on calculation milestones to avoid precision drift associated with floating-point approximations.

---

## Testing Strategy Paradigm

The codebase implements a comprehensive, multi-tiered testing matrix that focuses on validating actual business behaviors rather than verifying mocking side effects:

- **Deterministic Domain Unit Testing (`ShoppingCartTest`)**: Validates quantity aggregation correctness, normalization parameters, validation checks, and edge-case rounding behavior completely free of Mockito stubbing arrays.
- **High-Throughput Concurrency Verification (`ShoppingCartConcurrencyTest`)**: subjects a shared cart session instance to cross-threaded bombardment via `CountDownLatch` pipelines to ensure state integrity and thread safety under heavy concurrent loads.
- **Component Integration Verification (`HttpPriceApiGatewayIntegrationTest`)**: Verifies actual communication pathways against live remote servers, ensuring that downstream error boundaries map precisely to their designated REST exceptions.

### Executing the Testing Matrix
Run the comprehensive test suite locally using the following build command:
```bash
mvn clean test