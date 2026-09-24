# Scalable URL Shortener

A URL shortening service built with **Java 21, Spring Boot, PostgreSQL, Redis, and Docker**.

The project uses separate services for **URL creation** and **URL redirection**, with Redis caching to optimize the read-heavy redirect flow.

## Features

- Create short URLs from long URLs
- Generate compact short codes using **Base62 encoding**
- Support optional URL expiration
- Fast redirects using **Redis caching**
- Negative caching for non-existent short URLs
- Separate URL creation and redirect services
- PostgreSQL for persistent URL storage
- Dockerized PostgreSQL and Redis infrastructure
- Centralized error handling with appropriate HTTP responses
- Basic unit and application context tests

## Architecture

```text
                         ┌─────────────────────┐
                         │       Client        │
                         └──────────┬──────────┘
                                    │
                    ┌───────────────┴───────────────┐
                    │                               │
              Create URL                         Redirect
                    │                               │
                    ▼                               ▼
        ┌────────────────────┐          ┌────────────────────┐
        │ Create URL Service │          │  Redirect Service  │
        │     :8080          │          │       :8081        │
        └─────────┬──────────┘          └─────────┬──────────┘
                  │                               │
                  │                               ▼
                  │                         ┌───────────┐
                  │                         │   Redis   │
                  │                         └─────┬─────┘
                  │                               │
                  │                         Cache miss
                  │                               │
                  └──────────────┬────────────────┘
                                 ▼
                          ┌─────────────┐
                          │ PostgreSQL  │
                          └─────────────┘
```

### Services

**Create URL Service**
- Accepts long URLs and optional expiration dates
- Generates a unique database ID
- Converts the ID to a Base62 short code
- Stores the URL mapping in PostgreSQL

**Redirect Service**
- Receives a short code
- Checks Redis first
- Falls back to PostgreSQL on a cache miss
- Returns a `302 Found` redirect to the original URL

## Key Design Decisions

- **Base62 Short Codes** — Database IDs are converted to Base62 to generate short, URL-friendly codes.
- **Separate Services** — URL creation and redirection are separated so the read-heavy redirect service can be optimized independently.
- **Redis Caching** — Frequently accessed URLs are cached in Redis to reduce database queries and improve redirect performance.
- **URL Expiration** — URLs can optionally expire, returning `410 Gone` when expired and `404 Not Found` when the short code doesn't exist.

## API

### Create Short URL

`POST /api/v1/create-url`

```json
{
  "longUrl": "https://example.com/long-url",
  "expiresAt": "2026-12-31T23:59:59Z"
}
```

Returns the generated short URL and its short code.

### Redirect

`GET /{shortCode}`

Redirects the user to the original URL with a `302 Found` response.

## Running Locally

### Requirements

- Java 21
- Maven
- Docker

### 1. Start PostgreSQL and Redis

From the project root:

```bash
docker compose -f infra/docker-compose.yml up -d
```

This starts:

```text
PostgreSQL → localhost:5432
Redis      → localhost:6379
```

### 2. Start the Create URL Service

```bash
cd create-url-service
./mvnw spring-boot:run
```

Runs on:

```text
http://localhost:8080
```

### 3. Start the Redirect Service

```bash
cd redirect-service
./mvnw spring-boot:run
```

Runs on:

```text
http://localhost:8081
```

### 4. Create a URL

```bash
curl -X POST http://localhost:8080/api/v1/create-url \
  -H "Content-Type: application/json" \
  -d '{
    "longUrl": "https://example.com"
  }'
```

Then use the returned short code with the redirect service:

```text
http://localhost:8081/{shortCode}
```

## Tech Stack

| Technology | Purpose |
|---|---|
| Java 21 | Backend development |
| Spring Boot | REST services |
| Spring Data JPA | Database access |
| PostgreSQL 16 | Persistent URL mappings |
| Redis 7 | Redirect caching |
| Maven | Build & dependency management |
| Docker | Infrastructure & containerization |
| JUnit | Testing |

## Testing

Run the tests from each service:

```bash
./mvnw test
```


Built as a backend and system-design project to explore **service separation, URL generation, caching, persistence, and scalability**.
