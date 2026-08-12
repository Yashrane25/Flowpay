# Flowpay

**Real time digital wallet and peer-to-peer payment platform** built to demonstrate real financial system engineering: ACID compliant fund transfers, pessimistic concurrency control, an immutable double entry ledger, idempotent transaction processing and an event driven architecture, all secured behind cookie based JWT authentication with CSRF protection.

<div align="center">

[![Java](https://img.shields.io/badge/Java-21-007396?style=flat-square&logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3-6DB33F?style=flat-square&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Spring Security](https://img.shields.io/badge/Spring%20Security-JWT%20%2B%20RBAC-6DB33F?style=flat-square&logo=springsecurity&logoColor=white)](https://spring.io/projects/spring-security)
[![React](https://img.shields.io/badge/React-18-61DAFB?style=flat-square&logo=react&logoColor=white)](https://reactjs.org/)
[![Vite](https://img.shields.io/badge/Vite-Build%20Tool-646CFF?style=flat-square&logo=vite&logoColor=white)](https://vitejs.dev/)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1?style=flat-square&logo=mysql&logoColor=white)](https://www.mysql.com/)
[![Redis](https://img.shields.io/badge/Redis-Cache--Aside-DC382D?style=flat-square&logo=redis&logoColor=white)](https://redis.io/)
[![RabbitMQ](https://img.shields.io/badge/RabbitMQ-Event--Driven-FF6600?style=flat-square&logo=rabbitmq&logoColor=white)](https://www.rabbitmq.com/)
[![Flyway](https://img.shields.io/badge/Flyway-Migrations-CC0200?style=flat-square&logo=flyway&logoColor=white)](https://flywaydb.org/)
[![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?style=flat-square&logo=docker&logoColor=white)](https://www.docker.com/)
[![Swagger](https://img.shields.io/badge/Swagger-OpenAPI-85EA2D?style=flat-square&logo=swagger&logoColor=black)](https://swagger.io/)

</div>

---

## Core Engineering Highlights

- **ACID compliant fund transfers** using `@Transactional` combined with **pessimistic row locking** (`SELECT ... FOR UPDATE`) with **deterministic lock ordering** to eliminate deadlocks under concurrent transfers.
- **Double entry ledger system** - every transfer writes an immutable, append only pair of `DEBIT`/`CREDIT` ledger rows, so wallet balances are always independently reconcilable against the ledger.
- **Idempotent transfer processing** - client generated `Idempotency-Key` headers, deduplicated via a database enforced `UNIQUE` constraint, so retried or duplicated requests never double charge.
- **Explicit transaction isolation** (`REPEATABLE_READ`) declared in code rather than relying on an implicit database default, documenting the exact guarantee the transfer engine depends on.
- **Cookie based JWT authentication** - tokens are issued as `httpOnly` cookies, paired with the double submit CSRF cookie pattern for state changing requests.
- **Role based access control (RBAC)** via Spring Method Security (`@PreAuthorize`), correctly distinguishing `401 Unauthorized` from `403 Forbidden`.
- **Per user rate limiting** on the transfer endpoint using the token bucket algorithm (Bucket4j).
- **Redis cache aside pattern** for wallet balance reads, with explicit cache eviction wired directly into the transfer write path to prevent stale balance reads.
- **Event driven notification pipeline** via RabbitMQ the transfer engine publishes a `TransferCompletedEvent` and returns immediately a fully decoupled consumer processes notifications independently.
- **Flyway managed schema migrations** - versioned, reviewable SQL migration files, not implicit auto DDL.
- **Automated test suite** - Mockito based unit tests for business logic, plus a `@SpringBootTest` integration test using `CountDownLatch`.
- **Fully Dockerized** backend + MySQL via a multi-stage `Dockerfile` and `docker-compose.yml`.
- **Auto generated, interactive API documentation** via Springdoc/Swagger UI.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Backend | Java 21, Spring Boot 3.3, Spring Security, Spring Data JPA |
| Database | MySQL 8.0, Flyway |
| Caching | Redis (cache aside pattern) |
| Messaging | RabbitMQ (producer/consumer, event driven) |
| Auth | JWT (httpOnly cookies), BCrypt, RBAC |
| Frontend | React 18, Vite, React Router |
| Testing | JUnit 5, Mockito |
| Docs | Springdoc OpenAPI / Swagger UI |
| Infra | Docker, Docker Compose |

---

## Architecture Flow

### Request Lifecycle (Authenticated Endpoint)

```
Client (React, credentials: 'include')
   │
   ▼
JwtAuthFilter        → reads httpOnly JWT cookie, validates signature + expiry,
                        populates SecurityContext with identity + role
   │
   ▼
RateLimitFilter       → per user token bucket check
   │
   ▼
Spring Security authorization  → .anyRequest().authenticated() / @PreAuthorize role checks
   │
   ▼
Controller  →  Service  →  Repository  →  MySQL
```

### Fund Transfer - Full Path

```
POST /api/transfer  { toWalletId, amount }  +  Idempotency Key header
   │
   ▼
TransferController  → resolves senders wallet from the AUTHENTICATED SESSION
   │
   ▼
TransferService.transfer()   [@Transactional, isolation = REPEATABLE_READ]
   │
   ├── 1. Idempotency check - has this exact key already completed?
   │        → return the original stored response.
   │
   ├── 2. Reserve the idempotency key via a DB enforced UNIQUE constraint
   │        → a genuinely concurrent duplicate request fails safely here
   │
   ├── 3. Lock BOTH wallets via SELECT ... FOR UPDATE, always in a
   │        deterministic order (lower wallet ID first) to prevent deadlocks
   │
   ├── 4. Validate sufficient balance
   │
   ├── 5. Debit sender / credit receiver, persist both wallets
   │
   ├── 6. Write an immutable Transaction + two LedgerEntry rows
   │        (one DEBIT, one CREDIT) - the permanent audit trail
   │
   ├── 7. Evict BOTH wallets' cached balances from Redis
   │        (forces the next read to refetch the correct and fresh value)
   │
   ├── 8. Publish a TransferCompletedEvent to RabbitMQ and return
   │        immediately - the HTTP response does NOT wait on this
   │
   ▼
Response returned to client
   │
   ▼ (fully decoupled, asynchronous - happens independently)
RabbitMQ  →  NotificationListener consumes the event  →  notification logic runs
```

### Wallet Balance Read - Cache Aside Path

```
GET /api/wallet/me
   │
   ▼
WalletService.getBalance(walletId)
   │
   ├── Check Redis: GET wallet:balance:{id}
   │
   ├── CACHE HIT  → return immediately, MySQL is never touched
   │
   └── CACHE MISS → query MySQL → store result in Redis → return the value
```

---

## API Documentation

Once the backend is running, explore every endpoint interactively at:

```
http://localhost:8080/swagger-ui.html
```

---

## Local Setup Guide

### Prerequisites

- Java 21 (JDK)
- Node.js 18+ and npm
- Docker Desktop
- Git

### Option A - Run Everything via Docker Compose

This spins up MySQL and the backend together, fully containerized.

```bash
git clone https://github.com/yashrane25/Flowpay.git
cd FlowPay
docker compose up --build
```

Wait for the logs to show `Tomcat started on port(s): 8080`. The backend is now live at `http://localhost:8080`.

Run the frontend:

```bash
cd flowpay-frontend
npm install
npm run dev
```

Open `http://localhost:5173` in your browser.

### Option B - Run Backend Natively (Without Docker)

1. **Create MySQL DB:**
```sql
   CREATE DATABASE flowpay_db;
```

2. **Set required environment variables**
```bash
   # Windows PowerShell
   [System.Environment]::SetEnvironmentVariable('DB_PASSWORD', 'your_mysql_password', 'User')
   [System.Environment]::SetEnvironmentVariable('JWT_SECRET', 'any-long-random-string', 'User')
```

3. **Start Redis and RabbitMQ:**
```bash
   docker run --name flowpay-redis -p 6379:6379 -d redis:7-alpine
   docker run --name flowpay-rabbitmq -p 5672:5672 -p 15672:15672 -d rabbitmq:3-management
```

4. **Run the backend** from `flowpay_backend/`:
```bash
   ./mvnw spring-boot:run
```

5. **Run the frontend**:
```bash
   cd flowpay-frontend
   npm install
   npm run dev
```

---

## Project Structure

```
FlowPay/
├── docker-compose.yml
├── flowpay_backend/
│   ├── src/main/java/com/yashrane/flowpay_backend/
│   │   ├── config/          # Security, Redis, RabbitMQ, OpenAPI config
│   │   ├── controller/      # REST endpoints
│   │   ├── dto/             # Request/response contracts
│   │   ├── entity/          # JPA entities
│   │   ├── event/           # RabbitMQ event payloads
│   │   ├── exception/       # Custom exceptions + centralized handler
│   │   ├── messaging/       # RabbitMQ producer/consumer
│   │   ├── repository/      # Spring Data JPA repositories
│   │   ├── security/        # JWT filter, rate limiting, CSRF
│   │   └── service/         # Business logic
│   └── src/main/resources/
│       └── db/migration/    # Flyway SQL migrations
└── flowpay-frontend/
    └── src/
        ├── api/              # Centralized fetch client, CSRF handling
        ├── context/          # Auth context (session derived, not token decoded)
        ├── components/       # Transfer form, transaction history
        └── pages/            # Login, Register, Dashboard
```

---

<div align="center">

⭐ Star this repository if you like it.

</div>
