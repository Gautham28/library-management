# Library Management System

Production-oriented REST API for managing library books, built with **Java 21**, **Spring Boot 3**, **PostgreSQL**, **Spring Data JPA**, and **Swagger/OpenAPI**.

## Features

- Full CRUD for books
- Search by title, author, genre, and ISBN
- Pagination and sorting
- Jakarta Validation on request payloads
- Global exception handling with consistent error responses
- Interactive API docs (Swagger UI)
- Dev / prod configuration profiles

## Architecture

```
Controller → Service → Repository → Entity (PostgreSQL)
                ↘ Mapper ↔ DTOs
```

| Layer | Responsibility |
|-------|----------------|
| Controller | HTTP mapping, status codes, `@Valid` |
| Service | Business rules, transactions |
| Repository | Data access (Spring Data JPA) |
| Entity | Database mapping |
| DTO | API request/response contracts |

## Prerequisites

- Java 21+
- Maven 3.9+ (or use `./mvnw`)
- PostgreSQL 14+ running locally

## Database setup

```bash
createdb library_db
# or:
psql -U postgres -c "CREATE DATABASE library_db;"
```

Optional: copy `.env.example` and export variables in your shell:

```bash
cp .env.example .env
export $(grep -v '^#' .env | xargs)
```

| Variable | Default | Purpose |
|----------|---------|---------|
| `DB_URL` | `jdbc:postgresql://localhost:5432/library_db` | JDBC URL |
| `DB_USERNAME` | `postgres` | DB user |
| `DB_PASSWORD` | `postgres` | DB password |
| `SPRING_PROFILES_ACTIVE` | `dev` | `dev` or `prod` |
| `SERVER_PORT` | `8080` | HTTP port |

## Run the application

```bash
./mvnw spring-boot:run
```

Or with an explicit profile:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
./mvnw spring-boot:run -Dspring-boot.run.profiles=prod
```

## Useful URLs

| URL | Description |
|-----|-------------|
| http://localhost:8080/swagger-ui.html | Interactive API docs |
| http://localhost:8080/v3/api-docs | OpenAPI JSON |
| http://localhost:8080/actuator/health | Health check |
| http://localhost:8080/api/v1/hello | Smoke endpoint |
| http://localhost:8080/api/v1/books | Books API |

## API quick start

```bash
# Create a book
curl -X POST http://localhost:8080/api/v1/books \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Clean Code",
    "author": "Robert C. Martin",
    "genre": "Software",
    "isbn": "978-0132350884",
    "publisher": "Prentice Hall",
    "publishedYear": 2008,
    "totalCopies": 10,
    "availableCopies": 10,
    "price": 39.99,
    "description": "A handbook of agile software craftsmanship"
  }'

# List / search (paginated)
curl "http://localhost:8080/api/v1/books?title=clean&page=0&size=10&sort=title,asc"

# Get by id
curl http://localhost:8080/api/v1/books/1

# Update
curl -X PUT http://localhost:8080/api/v1/books/1 \
  -H "Content-Type: application/json" \
  -d '{ ...same fields as create... }'

# Delete
curl -X DELETE http://localhost:8080/api/v1/books/1
```

### HTTP status codes

| Status | When |
|--------|------|
| 201 | Book created |
| 200 | Successful read/update |
| 204 | Successful delete |
| 400 | Validation or invalid book data |
| 404 | Book not found |
| 409 | Duplicate ISBN |
| 500 | Unexpected server error |

## Profiles

| Profile | `ddl-auto` | SQL logging | Swagger | Health details |
|---------|------------|-------------|---------|----------------|
| `dev` (default) | `update` | on | on | always |
| `prod` | `validate` | off | off | never |

**Why `validate` in prod?** Schema changes should come from migrations (Flyway/Liquibase), not Hibernate auto-update.

## Tests

Tests use an in-memory H2 database — PostgreSQL is not required.

```bash
./mvnw test
```

## Project structure

```
com.library.management
├── config/          # OpenAPI, request logging
├── controller/      # REST endpoints
├── service/         # Business logic
├── repository/      # Spring Data JPA
├── entity/          # JPA entities
├── dto/             # Request/response DTOs
├── mapper/          # Entity ↔ DTO
├── exception/       # Custom exceptions + @ControllerAdvice
└── enums/           # BookStatus
```

## Learning path (phases completed)

1. Project foundation (Maven, Spring Boot, hello endpoint)
2. PostgreSQL + JPA entity
3. Repository (search, pagination, sorting)
4. DTOs + validation + mapper
5. Service layer + business rules
6. REST controller
7. Global exception handling
8. Swagger / OpenAPI
9. Profiles, logging, README, production hygiene
