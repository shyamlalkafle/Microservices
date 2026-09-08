# Microservices Project

A Spring Boot microservices architecture demonstrating service-to-service communication, proper DTO usage, comprehensive error handling, and resilience patterns.

## 📋 Overview

This project implements two microservices:
- **UserService**: Manages user information (CRUD operations)
- **OrderService**: Manages orders with validation against UserService

Key features:
- ✅ HTTP-based inter-service communication
- ✅ Independent databases (H2 in-memory)
- ✅ DTO layer for API contracts
- ✅ Comprehensive error handling
- ✅ Service resilience (handles downstream failures)
- ✅ Externalized configuration

## 🛠 Tech Stack

- **Language**: Java 21
- **Framework**: Spring Boot 4.1.1
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
**Runs on:** http://localhost:8090

#### Terminal 2: Start OrderService
```powershell
cd OrderService
.\mvnw spring-boot:run
```
**Runs on:** http://localhost:8080

### Quick Test
```powershell
# Create a user
curl -X POST http://localhost:8090/api/users -H "Content-Type: application/json" -d '{\"name\": \"John Doe\", \"phoneNo\": \"1234567890\", \"address\": \"123 Main St\"}'

# Create an order
curl -X POST http://localhost:8080/api/orders -H "Content-Type: application/json" -d '{\"userId\": 1, \"productName\": \"Laptop\", \"quantity\": 2, \"totalPrice\": 2000.00}'
```

## 📁 Project Structure

```
Microservices/
├── UserService/
│   ├── src/main/java/com/microservices/UserService/
│   │   ├── controller/          # REST endpoints
│   │   ├── dto/                 # UserResponse DTO
│   │   ├── entity/              # User entity
│   │   ├── repository/          # JPA repository
│   │   ├── service/             # Business logic
│   │   └── UserServiceApplication.java
│   └── src/main/resources/
│       └── application.yaml     # Configuration
│
├── OrderService/
│   ├── src/main/java/com/microservices/OrderService/
│   │   ├── config/              # WebClient configuration
│   │   ├── controller/          # REST endpoints
│   │   ├── dto/                 # OrderResponse, ErrorResponse DTOs
│   │   ├── entity/              # Order entity
│   │   ├── exception/           # Custom exceptions & global handler
│   │   ├── repository/          # JPA repository
│   │   ├── service/             # Business logic + UserServiceClient
│   │   └── OrderServiceApplication.java
│   └── src/main/resources/
│       └── application.yaml     # Configuration
│
├── ARCHITECTURE.md              # Architecture diagrams & patterns
├── IMPLEMENTATION_SUMMARY.md    # Detailed implementation guide
├── TESTING_GUIDE.md             # Comprehensive testing scenarios
├── QUICK_START.md               # Fast setup guide
└── README.md                    # This file
```

## 📚 Documentation

| Document | Description |
|----------|-------------|
| [ARCHITECTURE.md](ARCHITECTURE.md) | System architecture, diagrams, design patterns |
| [IMPLEMENTATION_SUMMARY.md](IMPLEMENTATION_SUMMARY.md) | Step-by-step implementation details & rationale |
| [TESTING_GUIDE.md](TESTING_GUIDE.md) | Complete testing scenarios including failure cases |
| [QUICK_START.md](QUICK_START.md) | Quick setup and basic commands |

## 🎯 Key Features

### 1. Service-to-Service Communication
OrderService validates user existence by making HTTP calls to UserService before creating orders.

### 2. DTOs (Data Transfer Objects)
- **UserResponse**: Exposes only intended user fields
- **OrderResponse**: Keeps user reference separate (userId only, not full User object)
- **ErrorResponse**: Consistent error format across all endpoints

### 3. Error Handling
Comprehensive error handling with proper HTTP status codes:
- **400 Bad Request**: User doesn't exist
- **404 Not Found**: Resource not found
- **502 Bad Gateway**: Downstream service error
- **503 Service Unavailable**: Downstream service down

### 4. Service Resilience
- Timeout configuration (5 seconds)
- Graceful handling of downstream failures
- No stack traces exposed to clients
- Service recovers automatically when dependencies return

### 5. Configuration Externalization
- Service URLs in `application.yaml`
- Easy port changes without code modifications
- Ready for environment-specific configurations

## 🧪 Testing

### Access H2 Consoles
- **UserService**: http://localhost:8090/h2-console
- **OrderService**: http://localhost:8080/h2-console
  - JDBC URL: `jdbc:h2:mem:microservices`
  - Username: `sa`
  - Password: (empty)

### Run Complete Tests
Follow the [TESTING_GUIDE.md](TESTING_GUIDE.md) to test:
- Happy path (normal flow)
- User not found scenario
- Service unavailable scenario
- Configuration externalization

### Test Service Resilience
```powershell
# Stop UserService (Ctrl+C), then try to create an order
curl -X POST http://localhost:8080/api/orders -H "Content-Type: application/json" -d '{\"userId\": 1, \"productName\": \"Phone\", \"quantity\": 1, \"totalPrice\": 500.00}'

# Should return 503 Service Unavailable with clear message
```

## 🏗 Architecture Highlights

### Microservices Principles
- ✅ **Single Responsibility**: Each service manages one domain
- ✅ **Loose Coupling**: HTTP communication, no shared database
- ✅ **Independent Deployment**: Services can be deployed separately
- ✅ **Database per Service**: Each has its own H2 instance
- ✅ **Resilience**: Handles downstream failures gracefully

### Design Patterns Used
- **DTO Pattern**: API contract separation
- **Repository Pattern**: Data access abstraction
- **Service Pattern**: Business logic layer
- **Global Exception Handling**: Centralized error management
- **Client-Side Discovery**: Configured service URLs

## 🔧 API Endpoints

### UserService (Port 8090)
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | /api/users | Get all users |
| GET | /api/users/{id} | Get user by ID |
| GET | /api/users/{id}/exists | Check if user exists |
| POST | /api/users | Create new user |
| PUT | /api/users/{id} | Update user |
| DELETE | /api/users/{id} | Delete user |

### OrderService (Port 8080)
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | /api/orders | Get all orders |
| GET | /api/orders/{id} | Get order by ID |
| POST | /api/orders | Create new order (validates user) |
| DELETE | /api/orders/{id} | Delete order |

## 🚦 HTTP Status Codes

| Code | Meaning | When |
|------|---------|------|
| 200 | OK | Successful GET request |
| 201 | Created | Successful POST (resource created) |
| 400 | Bad Request | User doesn't exist / invalid input |
| 404 | Not Found | Resource not found |
| 502 | Bad Gateway | Downstream service returned error |
| 503 | Service Unavailable | Downstream service is down |

## 🔮 Future Enhancements

- [ ] Add Eureka for service discovery
- [ ] Add Spring Cloud Gateway as API Gateway
- [ ] Implement Resilience4j circuit breaker
- [ ] Add distributed tracing (Sleuth + Zipkin)
- [ ] Implement Spring Cloud Config Server
- [ ] Add authentication (Spring Security + JWT)
- [ ] Replace H2 with PostgreSQL
- [ ] Add Docker support
- [ ] Add Swagger/OpenAPI documentation
- [ ] Add integration tests

## 📝 License

This is a learning project demonstrating microservices architecture patterns.

## 👤 Author

Shyamlal Kafle

---

**Ready to explore?** Start with [QUICK_START.md](QUICK_START.md) or dive deep into [ARCHITECTURE.md](ARCHITECTURE.md)!