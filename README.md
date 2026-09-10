# Microservices Project

A Spring Boot microservices architecture demonstrating service-to-service communication, API Gateway pattern, circuit breaker resilience, proper DTO usage, comprehensive error handling, and distributed transaction coordination.

## 📋 Overview

This project implements a complete microservices ecosystem:
- **API Gateway**: Central entry point with routing, circuit breaker, and fallback handling
- **UserService**: Manages user information (CRUD operations + balance management)
- **OrderService**: Manages orders with validation and payment orchestration
- **PaymentService**: Handles payment processing and user balance deduction

Key features:
- ✅ API Gateway with Spring Cloud Gateway
- ✅ Circuit Breaker pattern with fallback mechanisms
- ✅ HTTP-based inter-service communication
- ✅ Independent databases (H2 in-memory)
- ✅ DTO layer for API contracts
- ✅ Comprehensive error handling
- ✅ Service resilience (handles downstream failures)
- ✅ Distributed transaction coordination
- ✅ Externalized configuration

## 🛠 Tech Stack

- **Language**: Java 21
- **Framework**: Spring Boot 4.1.1
- **API Gateway**: Spring Cloud Gateway
- **Resilience**: Circuit Breaker with fallback handlers
- **Database**: H2 (in-memory)
- **Build Tool**: Maven
- **HTTP Client**: WebClient (Spring WebFlux)
- **Other**: Spring Data JPA, Lombok

## 🚀 Getting Started

### Prerequisites
- Java 21 or higher
- Maven (or use included `mvnw`)
- PowerShell (Windows) or Bash (Linux/Mac)

### Installation
```bash
git clone https://github.com/shyamlalkafle/Microservices.git
cd Microservices
```

### Quick Start

**See [QUICK_START.md](QUICK_START.md) for fast setup and basic testing.**

#### Terminal 1: Start UserService
```powershell
cd UserService
.\mvnw spring-boot:run
```
**Runs on:** http://localhost:8081

#### Terminal 2: Start PaymentService
```powershell
cd PaymentServices
.\mvnw spring-boot:run
```
**Runs on:** http://localhost:8083

#### Terminal 3: Start OrderService
```powershell
cd OrderService
.\mvnw spring-boot:run
```
**Runs on:** http://localhost:8082

#### Terminal 4: Start API Gateway
```powershell
cd ApiGateway
.\mvnw spring-boot:run
```
**Runs on:** http://localhost:8080

### Testing
Use the included Postman collection to test all endpoints through the API Gateway.

## 📁 Project Structure

```
Microservices/
├── ApiGateway/
│   ├── src/main/java/com/microservices/ApiGateway/
│   │   ├── controller/          # FallbackController (circuit breaker fallbacks)
│   │   ├── dto/                 # ErrorResponse DTO
│   │   ├── exception/           # GatewayExceptionHandler
│   │   └── ApiGatewayApplication.java
│   └── src/main/resources/
│       └── application.yaml     # Routes, Circuit Breaker config
│
├── UserService/
│   ├── src/main/java/com/microservices/UserService/
│   │   ├── controller/          # REST endpoints
│   │   ├── dto/                 # UserResponse, BalanceDeductRequest DTOs
│   │   ├── entity/              # User entity (with balance)
│   │   ├── repository/          # JPA repository
│   │   ├── service/             # Business logic
│   │   └── UserServiceApplication.java
│   └── src/main/resources/
│       └── application.yaml     # Configuration
│
├── PaymentServices/
│   ├── src/main/java/com/microservices/PaymentServices/
│   │   ├── config/              # WebClient configuration
│   │   ├── controller/          # REST endpoints
│   │   ├── dto/                 # PaymentRequest, PaymentResponse DTOs
│   │   ├── entity/              # Payment entity
│   │   ├── enums/               # PaymentStatus enum
│   │   ├── repository/          # JPA repository
│   │   ├── service/             # Business logic + UserServiceClient
│   │   └── PaymentServicesApplication.java
│   └── src/main/resources/
│       └── application.yaml     # Configuration
│
├── OrderService/
│   ├── src/main/java/com/microservices/OrderService/
│   │   ├── config/              # WebClient configuration
│   │   ├── controller/          # REST endpoints
│   │   ├── dto/                 # OrderRequest, OrderResponse, PaymentRequest DTOs
│   │   ├── entity/              # Order entity
│   │   ├── enums/               # OrderStatus enum
│   │   ├── exception/           # Custom exceptions & global handler
│   │   ├── repository/          # JPA repository
│   │   ├── service/             # Business logic + UserServiceClient + PaymentServiceClient
│   │   └── OrderServiceApplication.java
│   └── src/main/resources/
│       └── application.yaml     # Configuration
│
└── README.md                    # This file
```

## 🎯 Key Features

### 1. API Gateway Pattern
Centralized entry point for all client requests with:
- **Routing**: Intelligent request routing to appropriate microservices
- **Circuit Breaker**: Automatic failure detection and fallback responses
- **Resilience**: Graceful degradation when services are unavailable
- **Centralized Error Handling**: Consistent error responses across all routes

### 2. Payment Flow Orchestration
Complete payment transaction flow:

```
┌─────────────────────────────────────────────────────────────┐
│                          CLIENT                              │
└───────────────────────────────┬───────────────────────────────┘
                                 │ POST /api/orders
                                 ▼
┌─────────────────────────────────────────────────────────────┐
│                       API GATEWAY                            │
└───────────────────────────────┬───────────────────────────────┘
                                 ▼
┌─────────────────────────────────────────────────────────────┐
│                       ORDER SERVICE                           │
│                                                               │
│  1. Check user exists  ──────────────► USER SERVICE          │
│     (GET /api/users/{id}/exists)       (checks user table)   │
│                                                               │
│     user not found?  ──► throw UserNotFoundException ──► 400 │
│                                                               │
│  2. Create Order (status = PENDING)                          │
│     Save to DB  ──────────────► order gets an ID             │
│                                                               │
│  3. Call Payment Service  ────────────► PAYMENT SERVICE      │
│     (POST /api/payments: orderId, userId, amount)            │
└───────────────────────────────┬───────────────────────────────┘
                                 ▼
┌─────────────────────────────────────────────────────────────┐
│                      PAYMENT SERVICE                          │
│                                                               │
│  1. Create Payment (status = PENDING)                        │
│     Save to DB                                                │
│                                                               │
│  2. Call User Service  ───────────────► USER SERVICE         │
│     (PATCH /api/users/{id}/deduct-balance?amount=X)          │
│                                                               │
│     balance < amount?  ──► return false ──► status = FAILED  │
│     balance >= amount? ──► deduct + save ──► status = SUCCESS│
│                                                               │
│  3. Save Payment (final status)                              │
│     Return status to Order Service                           │
└───────────────────────────────┬───────────────────────────────┘
                                 ▼
┌─────────────────────────────────────────────────────────────┐
│                       ORDER SERVICE                           │
│                                                               │
│  4. Payment SUCCESS?  ──► Order.status = CONFIRMED            │
│     Payment FAILED?   ──► Order.status = FAILED               │
│                                                               │
│  5. Save final Order status                                  │
│     Return OrderResponse to Client                           │
└───────────────────────────────┬───────────────────────────────┘
                                 ▼
┌─────────────────────────────────────────────────────────────┐
│                          CLIENT                              │
│              (receives final order + status)                 │
└─────────────────────────────────────────────────────────────┘
```

### 3. Circuit Breaker & Fallback Scenarios
API Gateway handles service failures with different fallback strategies:

|Scenario|Route in YAML?|CircuitBreaker filter?|Fallback method exists?|Status code|Response body|
|---|---|---|---|---|---|
|1|✅ Yes|✅ Yes|✅ Yes|`503`|Clean custom JSON from `FallbackController` (e.g. `"User service is temporarily unavailable..."`)|
|2|✅ Yes|✅ Yes|❌ No|`500`|`{"error": "Internal Server Error"}` — gateway can't find the fallback endpoint, falls through to Boot's default handler|
|3|✅ Yes|❌ No filter|—|`503`|Your `ErrorResponse` JSON (`status`, `error`, `message`, `path`, `timestamp`) — from `GatewayExceptionHandler`, since it correctly detects `ConnectException`|
|4|❌ No route matches at all|—|—|`500`|`{"error": "Internal Server Error"}` — this is your original `/api/notification` (before you added the route) case; the "route not found" exception type isn't a `ConnectException`, so `GatewayExceptionHandler`'s `isConnectionRefused()` check returns `false`, falls into the generic `else` branch — but even that branch's output apparently wasn't reaching the client, meaning something upstream (Spring Cloud Gateway's own routing failure) intercepted it before your handler ever ran|
|5 (with catch-all route)|✅ Yes (`/api/**` last)|✅ Yes|✅ Yes (`service-not-found`)|`503`|Clean generic fallback JSON (`"No matching service found..."`) — this is what fixes scenario 4|

### 4. Service-to-Service Communication
- OrderService validates users via UserService
- OrderService orchestrates payment via PaymentService
- PaymentService deducts balance via UserService
- All communication uses WebClient with proper timeout and error handling

### 5. DTOs (Data Transfer Objects)
- **UserResponse**: Exposes only intended user fields
- **OrderRequest/Response**: Clean API contracts with status tracking
- **PaymentRequest/Response**: Payment processing contracts
- **ErrorResponse**: Consistent error format across all services

### 6. Error Handling
Comprehensive error handling with proper HTTP status codes:
- **400 Bad Request**: User doesn't exist / invalid input / insufficient balance
- **404 Not Found**: Resource not found
- **502 Bad Gateway**: Downstream service error
- **503 Service Unavailable**: Downstream service down / circuit breaker open

### 7. Service Resilience
- Timeout configuration (5 seconds)
- Circuit breaker with fallback handlers
- Graceful handling of downstream failures
- No stack traces exposed to clients
- Services recover automatically when dependencies return

### 8. Configuration Externalization
- Service URLs in `application.yaml`
- Easy port changes without code modifications
- Ready for environment-specific configurations

## 🧪 Testing

### Access H2 Consoles
- **UserService**: http://localhost:8081/h2-console
- **OrderService**: http://localhost:8082/h2-console
- **PaymentService**: http://localhost:8083/h2-console
  - JDBC URL: `jdbc:h2:mem:microservices`
  - Username: `sa`
  - Password: (empty)

### API Testing
Use the included **Postman collection** to test all endpoints through the API Gateway.

### Test Scenarios
Follow the [TESTING_GUIDE.md](TESTING_GUIDE.md) to test:
- Happy path (normal flow with payment)
- User not found scenario
- Insufficient balance scenario
- Service unavailable scenario
- Circuit breaker and fallback behavior
- Configuration externalization

### Testing Service Resilience
Stop any backend service (UserService, OrderService, or PaymentService) to see:
- Circuit breaker activation
- Fallback responses
- Graceful degradation

## 🏗 Architecture Highlights

### Microservices Principles
- ✅ **Single Responsibility**: Each service manages one domain
- ✅ **API Gateway Pattern**: Central entry point with routing and resilience
- ✅ **Loose Coupling**: HTTP communication, no shared database
- ✅ **Independent Deployment**: Services can be deployed separately
- ✅ **Database per Service**: Each has its own H2 instance
- ✅ **Resilience**: Circuit breaker and graceful degradation
- ✅ **Transaction Coordination**: Distributed transaction handling across services

### Design Patterns Used
- **API Gateway Pattern**: Centralized routing and cross-cutting concerns
- **Circuit Breaker Pattern**: Automatic failure detection and recovery
- **DTO Pattern**: API contract separation
- **Repository Pattern**: Data access abstraction
- **Service Pattern**: Business logic layer
- **Global Exception Handling**: Centralized error management
- **Client-Side Load Balancing**: Configured service URLs
- **Saga Pattern**: Distributed transaction coordination (Order → Payment → User)

## 🔧 API Endpoints

### API Gateway Routing (Port 8080)
All client requests should go through the API Gateway, which routes to backend services:

```
                        ┌─ > user-service-route    uri -> http://localhost:8081
client ── api-gateway ──┼─ > order-service-route   uri -> http://localhost:8082
                        ├─ > payment-service-route uri -> http://localhost:8083
                        └─ > catch-all-fallback     uri -> http://localhost:9999 (always fails, triggers generic fallback)
```

**Route Patterns:**
- `/api/users/**` → UserService (8081)
- `/api/orders/**` → OrderService (8082)
- `/api/payments/**` → PaymentService (8083)
- `/api/**` → Catch-all fallback (generic error message)

### UserService (Port 8081)
**Direct access (for development/testing only):**

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | /api/users | Get all users |
| GET | /api/users/{id} | Get user by ID |
| GET | /api/users/{id}/exists | Check if user exists |
| POST | /api/users | Create new user |
| PUT | /api/users/{id} | Update user |
| PATCH | /api/users/{id}/deduct-balance?amount={amount} | Deduct balance from user |
| DELETE | /api/users/{id} | Delete user |

### OrderService (Port 8082)
**Direct access (for development/testing only):**

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | /api/orders | Get all orders |
| GET | /api/orders/{id} | Get order by ID |
| POST | /api/orders | Create new order (validates user + processes payment) |
| DELETE | /api/orders/{id} | Delete order |

### PaymentService (Port 8083)
**Direct access (for development/testing only):**

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | /api/payments | Get all payments |
| GET | /api/payments/{id} | Get payment by ID |
| POST | /api/payments | Process payment (deducts user balance) |

## 🚦 HTTP Status Codes

| Code | Meaning | When |
|------|---------|------|
| 200 | OK | Successful GET request |
| 201 | Created | Successful POST (resource created) |
| 400 | Bad Request | User doesn't exist / invalid input / insufficient balance |
| 404 | Not Found | Resource not found |
| 500 | Internal Server Error | Unhandled error / route not configured |
| 502 | Bad Gateway | Downstream service returned error |
| 503 | Service Unavailable | Downstream service is down / Circuit breaker open |

## 🔄 Order Status Flow

Orders progress through these states:
1. **PENDING**: Initial state when order is created
2. **CONFIRMED**: Payment successful, order confirmed
3. **FAILED**: Payment failed (insufficient balance or payment error)

## 💳 Payment Status Flow

Payments progress through these states:
1. **PENDING**: Initial state when payment is created
2. **SUCCESS**: Balance deducted successfully
3. **FAILED**: Insufficient balance or deduction error

## 🌐 Service Ports

| Service | Port | Purpose |
|---------|------|---------|
| API Gateway | 8080 | **Main entry point** - Route all client requests here |
| UserService | 8081 | User management and balance operations |
| OrderService | 8082 | Order management and orchestration |
| PaymentService | 8083 | Payment processing |

**Best Practice**: Always use API Gateway (port 8080) for production-like testing. Direct service access is for development/debugging only.

## 🎓 Learning Outcomes

This project demonstrates:
- ✅ Building a complete microservices ecosystem
- ✅ Implementing API Gateway with Spring Cloud Gateway
- ✅ Circuit breaker pattern and resilience engineering
- ✅ Distributed transaction coordination (Saga pattern)
- ✅ Service-to-service communication
- ✅ Proper error handling and propagation
- ✅ DTO usage for clean API contracts
- ✅ Database per service pattern
- ✅ Configuration management
- ✅ RESTful API design

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## 📄 License

This project is open source and available for educational purposes.
