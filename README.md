# Shipping Service

A production-grade Spring Boot microservice responsible for **shipping cost calculation** and **address lookup** via CEP (Brazilian postal code). This service does **not** manage carts — it receives cart data externally and focuses solely on logistics calculations.

---

## 1. Project Overview

This service provides two core capabilities:

- **Address Resolution**: Translates a Brazilian CEP into a structured address (street, city, state) using the [ViaCEP](https://viacep.com.br) public API.
- **Shipping Calculation**: Determines shipping cost based on cart subtotal and destination state.

It is designed to be consumed by a Cart Service or any upstream system that already holds cart/order data.

---

## 2. Architecture

The project follows a **layered architecture**:

```
src/main/java/com/izanami/management_shipping/
├── config/          → Configuration beans (RestTemplate, OpenAPI, properties)
├── client/          → External API clients (ViaCEP)
├── controller/      → REST endpoints
├── service/         → Business logic
├── dto/             → Data Transfer Objects
└── exception/       → Custom exceptions and global error handler
```

| Layer       | Responsibility                                  |
|-------------|--------------------------------------------------|
| Controller  | Handles HTTP requests and delegates to services  |
| Service     | Contains business rules and orchestration        |
| Client      | Communicates with external APIs (ViaCEP)         |
| DTO         | Defines request/response contracts               |
| Exception   | Centralized error handling                       |
| Config      | Application configuration and beans              |

---

## 3. Shipping Rules

| Condition                          | Shipping Cost |
|------------------------------------|---------------|
| Subtotal ≥ R$200.00               | **Free** (R$0.00) |
| Same state as origin (SP)          | R$10.00       |
| Different state from origin (SP)   | R$20.00       |

- Origin state is configured in `application.yaml` (default: `SP`)
- Free shipping rule takes priority over state-based calculation

---

## 4. Address Integration (ViaCEP)

The service integrates with the public ViaCEP API:

```
GET https://viacep.com.br/ws/{cep}/json/
```

**Features:**
- Timeout configured via `viacep.timeout` property (default: 5000ms)
- Handles invalid CEPs (API returns `{"erro": true}`)
- Handles connection timeouts and API failures
- All external calls are logged (INFO/ERROR)

---

## 5. API Endpoints

### Address Lookup

```http
GET /address/{cep}
```

**Example:**
```bash
curl http://localhost:8080/address/01001000
```

**Response (200):**
```json
{
  "street": "Praça da Sé",
  "city": "São Paulo",
  "state": "SP",
  "cep": "01001-000"
}
```

---

### Shipping Calculation

```http
GET /shipping?subtotal={value}&cep={cep}&cartId={cartId}
```

| Parameter | Type       | Required | Description                    |
|-----------|------------|----------|--------------------------------|
| subtotal  | BigDecimal | Yes      | Cart subtotal (>= 0)          |
| cep       | String     | Yes      | Destination CEP (8 digits)     |
| cartId    | String     | No       | Cart ID for traceability       |

**Example — Same state:**
```bash
curl "http://localhost:8080/shipping?subtotal=100.00&cep=01001000&cartId=cart-123"
```

**Response (200):**
```json
{
  "shippingCost": 10.00,
  "destinationState": "SP",
  "freeShipping": false
}
```

**Example — Different state:**
```bash
curl "http://localhost:8080/shipping?subtotal=50.00&cep=20040020"
```

**Response (200):**
```json
{
  "shippingCost": 20.00,
  "destinationState": "RJ",
  "freeShipping": false
}
```

**Example — Free shipping:**
```bash
curl "http://localhost:8080/shipping?subtotal=250.00&cep=20040020"
```

**Response (200):**
```json
{
  "shippingCost": 0,
  "destinationState": "RJ",
  "freeShipping": true
}
```

---

## 6. Validation Rules

| Field    | Rule                                        | Error Code |
|----------|---------------------------------------------|------------|
| CEP      | Must be exactly 8 digits (after sanitization) | 400        |
| CEP      | Must correspond to a valid address          | 404        |
| Subtotal | Must be ≥ 0                                 | 400        |
| Subtotal | Required parameter                          | 400        |

CEP accepts both formats: `01001000` and `01001-000` (hyphen is stripped).

---

## 7. Error Handling

All errors return a standardized response:

```json
{
  "status": 400,
  "message": "Invalid CEP format: 123. Expected 8 digits.",
  "timestamp": "2025-01-15T10:30:00"
}
```

| HTTP Code | Scenario                          |
|-----------|-----------------------------------|
| 400       | Invalid CEP, negative subtotal    |
| 404       | CEP not found in ViaCEP           |
| 503       | ViaCEP timeout or unavailable     |
| 500       | Unexpected internal error         |

---

## 8. Known Limitations

- **No retry mechanism** — If ViaCEP fails, the request fails immediately
- **No caching** — Every request hits the ViaCEP API (repeated CEPs are not cached)
- **Depends on external cart data** — This service does not persist or validate cart existence
- **Single origin state** — Origin is hardcoded in configuration
- **No authentication** — Endpoints are publicly accessible

---

## 9. Running the Application

### Prerequisites
- Java 17+
- Maven 3.8+

### Build & Run
```bash
./mvnw clean install
./mvnw spring-boot:run
```

### Swagger UI
Once running, access the API documentation at:
```
http://localhost:8080/swagger-ui.html
```

### Run Tests
```bash
./mvnw test
```

---

## 10. Example Scenarios

### Scenario 1: Customer in São Paulo, small order
- Subtotal: R$80.00
- CEP: 01001000 (São Paulo, SP)
- Origin: SP
- **Result**: R$10.00 (same state)

### Scenario 2: Customer in Rio, medium order
- Subtotal: R$150.00
- CEP: 20040020 (Rio de Janeiro, RJ)
- Origin: SP
- **Result**: R$20.00 (different state)

### Scenario 3: Customer in Bahia, large order
- Subtotal: R$350.00
- CEP: 40010000 (Salvador, BA)
- Origin: SP
- **Result**: R$0.00 (free shipping, subtotal ≥ R$200)

---

## 11. Exercise Hooks (Future Improvements)

These are intentional extension points for learning or production hardening:

1. **Add caching for ViaCEP** — Use Spring Cache or Redis to avoid repeated calls for the same CEP
2. **Implement retry strategy** — Use Spring Retry or Resilience4j for transient failures
3. **Support multiple shipping tiers** — Add weight-based or distance-based pricing
4. **Integrate with Cart Service via REST** — Fetch cart subtotal by ID instead of receiving it as parameter
5. **Add circuit breaker** — Protect against prolonged ViaCEP outages

---

## 12. Tech Stack

| Technology       | Purpose                    |
|------------------|----------------------------|
| Java 17          | Language                   |
| Spring Boot 3.2  | Framework                  |
| Spring Web       | REST controllers           |
| Lombok           | Boilerplate reduction      |
| SpringDoc OpenAPI| Swagger documentation      |
| JUnit 5          | Unit testing               |
| Mockito          | Test mocking               |
| Maven            | Build tool                 |
