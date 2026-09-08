# Stateful Shopping Cart Subsystem

A production-grade, stateful shopping cart backend platform built using **Java 17** and **Spring Boot 3.x**.

The architecture separates the core business domain model from side-effect-heavy external network infrastructure layer boundaries. The platform orchestrates real-time product data lookup via a remote REST Pricing API and executes precise financial accounting routines natively leveraging `BigDecimal` with strict rounding controls.

---

## Key Features

* **Full-Stack Architecture**: Modern Angular 18+ frontend paired with a robust Spring Boot microservice backend.
* **Angular 18+ Signals State Management**: Modern reactive state management using Angular `signal()` and `inject()` functions.
* **Resilient Pricing Gateway**: Integration with external pricing APIs (`Equal Experts` catalog data).
* **Configurable Fallback Pricing**: Automatically resolves uncataloged or missing products (`404` errors) to a configurable default price set in `application.properties`.
* **Case-Insensitive Item Resolver**: Automatically normalizes product names prior to API execution.
* **Thread-Safe Concurrent Operations**: Verified state integrity under heavy cross-threaded loads.

---

## Architecture & System Design

graph TD
    subgraph Frontend["ANGULAR FRONTEND (Angular 18+)"]
        UI["CartComponent<br/>(Standalone UI Component)"]
        Service["CartService<br/>(Signals State Management & HttpClient)"]
        UI <--> Service
    end

    subgraph Backend["SPRING BOOT BACKEND (Java 17 / Spring Boot 3.x)"]
        Controller["CartController<br/>(REST API Gateway / Endpoints)"]
        Domain["ShoppingCart Domain<br/>(Stateful Business Orchestrator)"]
        Gateway["HttpPriceApiGateway<br/>(Resilient Integration Gateway)"]
        
        Controller --> Domain
        Controller --> Gateway
    end

    subgraph Upstream["EXTERNAL / FALLBACK SERVICES"]
        Catalog["Upstream Catalog API<br/>(Equal Experts Pricing Data)"]
        Fallback["Default Fallback Price<br/>(application.properties)"]
    end

    %% Communications
    Service -- "HTTP REST (JSON)" --> Controller
    Gateway -- "HTTP GET (JSON)" --> Catalog
    Gateway -. "404 Not Found Fallback" .-> Fallback


###Upstream Price Resolution Sequence

sequenceDiagram
    autonumber
    actor User
    participant UI as Angular UI / CartService
    participant Controller as CartController
    participant Domain as ShoppingCart
    participant Gateway as HttpPriceApiGateway
    participant Upstream as External Pricing API

    User->>UI: Add Product (e.g. "Apple")
    UI->>Controller: POST /api/v1/cart/{cartId}/items
    Controller->>Gateway: fetchPrice("apple")
    Gateway->>Upstream: GET /backend-take-home-test-data/apple.json
    
    alt Status 200 OK
        Upstream-->>Gateway: Return JSON { "price": 0.99 }
        Gateway-->>Controller: Return Optional.of(0.99)
    else Status 404 Not Found / Missing Item
        Upstream-->>Gateway: 404 Not Found
        Gateway-->>Controller: Fallback to Default Price (0.99)
    end

    Controller->>Domain: addItem(productName, price, quantity)
    Domain-->>Controller: Return Updated CartState (with 12.5% Tax)
    Controller-->>UI: 200 OK (Cart JSON Payload)
    UI-->>User: Update UI Signals & Display Balance

### System Component Layout

**1. Pure Domain Layer**
* **`ShoppingCart`**: The core stateful business orchestrator. It manages item quantity aggregation, handles input normalization metrics, and computes mathematical outputs natively.
* **`CartItem` & `CartState`**: Immutable Java records representing thread-safe transactional snapshots and financial breakdowns.

**2. External Integration Layer**
* **`HttpPriceApiGateway`**: A robust HTTP gateway powered by Java’s native `HttpClient` and Spring's `ObjectMapper`. It safely communicates with the remote vendor catalog, handles response telemetry, and manages protocol serialization boundaries with a fallback mechanism for missing items.

**3. API Controller Layer**
* **`CartController`**: A stateful REST API gateway providing fully documented, interactive endpoints. It acts as an orchestrator—handling client session state lookups, invoking the price gateway to fetch pricing definitions, and passing those definitions to the domain cart to compile the final state representation.

---

## User Interface Overview
The Angular UI provides a reactive, real-time interface for managing shopping cart:

<div align="center">
  <img src="assets/shopping-cart.png" style="max-width:100%; height:auto;" alt="Microscope Control Dashboard" />
 </div>

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

*Note: Supported live catalog items include `cheerio`, `cornflakes`, `frosties`, `shreddies`, and `weetabix`. Any uncataloged item automatically falls back to the configured default pricing.*

---

## Financial Accounting Parameters

- **Fixed Taxation Vector:** In absolute adherence to operational business rules, taxation metrics are evaluated at a fixed rate of **12.5%** (`0.125`).
- **Precision Scale Strategy:** All pricing vectors enforce a strict monetary resolution footprint of two decimal places, utilizing `RoundingMode.HALF_UP` on calculation milestones to avoid precision drift associated with floating-point approximations.

Running the Application
Backend (Spring Boot):

mvn clean spring-boot:run

Frontend (Angular):
cd shopping-cart-frontend
npm install
ng serve --open

## Testing Strategy Paradigm

The codebase implements a comprehensive, multi-tiered testing matrix focusing on business behaviors:

Deterministic Domain Unit Testing ( ShoppingCartTest) : Validates quantity aggregation correctness, normalization parameters, validation checks, and edge-case rounding behavior.

High-Throughput Concurrency Verification ( ShoppingCartConcurrencyTest) : Subjects a shared cart session instance to cross-threaded bombardment via CountDownLatchpipelines to ensure state integrity.

Component Integration Verification ( HttpPriceApiGatewayIntegrationTest) : Verifies communication pathways against live remote servers and validates default pricing fallback logic on 404 responses.

