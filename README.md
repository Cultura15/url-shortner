# Scalable URL Shortener

A URL shortening service built with **Java 21, Spring Boot, PostgreSQL, Redis, and Docker**.

The project is split into two independently deployable services: one for **creating** short URLs and one for **redirecting** them. Redirects are read-heavy, so that service sits behind a Redis cache and connects to the database with a read-only role.

## Features

- Create short URLs from long `http(s)` URLs
- Compact short codes using **Base62 encoding** of a database sequence ID
- Optional URL expiration (`410 Gone` once expired)
- Fast redirects using **Redis caching**, with TTL capped to the URL's expiry
- **Negative caching** for non-existent short codes
- Redirects keep working if Redis is down (falls back to PostgreSQL)
- **Per-IP rate limiting** on URL creation (10 requests/minute, Bucket4j)
- Least-privilege database access: the redirect service uses a **read-only** Postgres role
- Consistent JSON error responses
- Unit tests, integration tests, and a k6 rate-limit load test

## Services

| Service | Port | Responsibility | DB access |
|---|---|---|---|
| `create-url-service` | 8080 | Validates input, generates the ID and short code, stores the mapping, rate-limits callers | Read/write (`create_url_owner`) |
| `redirect-service` | 8081 | Looks up a short code (Redis first, then PostgreSQL) and returns a `302` | Read-only (`redirect_reader`) |

## File Structure

```text
url-shortener/
├── README.md                          # You are here
├── LICENSE                            # MIT license
│
├── create-url-service/                # WRITE side: creates short URLs (port 8080)
│   ├── Dockerfile                     
│   ├── pom.xml                        
│   ├── mvnw / mvnw.cmd                
│   └── src/
│       ├── main/
│       │   ├── java/com/jesson/create_url_service/
│       │   │   ├── CreateUrlServiceApplication.java   
│       │   │   ├── controller/UrlController.java      # POST /api/v1/create-url
│       │   │   ├── dto/                              
│       │   │   ├── entity/UrlEntity.java             
│       │   │   ├── exception/                         
│       │   │   ├── repository/UrlRepo.java           
│       │   │   ├── security/RateLimitFilter.java      
│       │   │   ├── service/UrlService.java            
│       │   │   └── utils/Base62.java                 
│       │   └── resources/application.properties       
│       └── test/                                      # Unit tests (Base62, validation, rate limit,
│                                                      
│
├── redirect-service/                  # READ side: resolves short codes (port 8081)
│   ├── Dockerfile                    
│   ├── pom.xml                      
│   ├── mvnw / mvnw.cmd               
│   └── src/
│       ├── main/
│       │   ├── java/com/jesson/redirect_service/
│       │   │   ├── RedirectServiceApplication.java    
│       │   │   ├── controller/RedirectController.java # GET /{shortCode} -> 302
│       │   │   ├── entity/UrlEntity.java             
│       │   │   ├── exception/                       
│       │   │   ├── repository/UrlRepo.java           
│       │   │   └── service/RedirectService.java      
│       │   └── resources/application.properties      
│       └── test/                                      # Unit tests (service, controller, handlers),
│                                                    
│
├── infra/                             # Local development infrastructure
│   ├── docker-compose.yml             
│   └── init-scripts/                 
│       └── 01create-roles.sql        
│
├── load-tests/
│   └── k6/rate-limit.js               # Verifies the 11th+ request in a minute gets a 429
│
└── scripts/dev/                       # Small helper scripts
    ├── database.sh                    # Opens a psql shell inside the Postgres container
    ├── junit-check.sh                 # Prints the resolved JUnit dependency version
    └── mockito-check.sh               # Prints the resolved Mockito dependency version
```

## How It Works

**Creating a URL**

1. Client sends `POST /api/v1/create-url` to the create service.
2. The rate limiter checks the caller's IP (10 requests per minute).
3. The request is validated (must be `http(s)`, not blank, length-limited, expiry in the future).
4. The service takes the next value from the `url_mapping_id_seq` Postgres sequence, so IDs are unique with no collision handling.
5. The ID is Base62-encoded into the short code (e.g. `62` becomes `10`) and the row is saved.

**Redirecting**

1. Client requests `GET /{shortCode}` on the redirect service.
2. The service checks Redis (`shortUrl:{code}`).
   - **Hit:** redirect immediately. A cached "not found" marker returns `404`.
   - **Miss or Redis down:** query PostgreSQL.
3. Result:
   - Found and active: cache it (TTL = time until expiry, capped at 24h) and return `302`.
   - Found but expired: `410 Gone`.
   - Not found: cache a short-lived "not found" marker and return `404`.

## API

### Create Short URL

`POST /api/v1/create-url` on the create service (default `http://localhost:8080`)

Request:

```json
{
  "longUrl": "https://example.com/some/long/path",
  "expiresAt": "2030-12-31T23:59:59Z"
}
```

`expiresAt` is optional (ISO-8601, must be in the future).

Response `201 Created`:

```json
{
  "shortUrl": "http://localhost:8081/1",
  "shortCode": "1",
  "originalUrl": "https://example.com/some/long/path",
  "expiresAt": "2030-12-31T23:59:59Z"
}
```

### Redirect

`GET /{shortCode}` on the redirect service (default `http://localhost:8081`)

Returns `302 Found` with a `Location` header pointing at the original URL.

### Status Codes

| Code | Service | Meaning |
|---|---|---|
| `201` | create | Short URL created |
| `302` | redirect | Redirecting to the original URL |
| `400` | create | Validation failed or malformed JSON |
| `404` | redirect | Short code does not exist |
| `405` | both | Wrong HTTP method |
| `409` | create | Data conflict (database constraint violation) |
| `410` | redirect | Short URL has expired |
| `429` | create | Rate limit exceeded |
| `503` | both | Database unavailable |
| `500` | both | Unexpected error |

Errors share one shape:

```json
{
  "timestamp": "2026-01-01T00:00:00Z",
  "status": 404,
  "error": "Not Found",
  "message": "URL not found for short code: abc"
}
```

### Health Check

Both services expose `GET /actuator/health`.

## Getting Started (Fresh Clone)

### Prerequisites

- **Java 21** (check with `java -version`)
- **Docker** with Docker Compose v2
- Maven is **not** required; each service ships with `./mvnw`
- (Optional) [k6](https://k6.io/) for the load test

### 1. Clone the repository

```bash
git clone https://github.com/cultura15/url-shortner.git
cd url-shortner
```

### 2. Start PostgreSQL and Redis

From the project root:

```bash
docker compose -f infra/docker-compose.yml up -d
```

On first start, Postgres automatically runs everything in `infra/init-scripts/` in order: it creates the `url_mapping` table and sequence, then the `create_url_owner` and `redirect_reader` roles.

| Component | Address |
|---|---|
| PostgreSQL | `localhost:5432` (db `urlshortener_db`) |
| Redis | `localhost:6379` |

Wait a few seconds for Postgres to become healthy:

```bash
docker compose -f infra/docker-compose.yml ps
```

### 3. Start the Create URL Service

In a new terminal:

```bash
cd create-url-service
./mvnw spring-boot:run
```

Runs on `http://localhost:8080`. On Windows use `mvnw.cmd spring-boot:run`.

### 4. Start the Redirect Service

In another terminal:

```bash
cd redirect-service
./mvnw spring-boot:run
```

Runs on `http://localhost:8081`.

### 5. Try it out

Create a short URL:

```bash
curl -X POST http://localhost:8080/api/v1/create-url \
  -H "Content-Type: application/json" \
  -d '{"longUrl": "https://example.com"}'
```

Follow the redirect (use the `shortCode` from the response):

```bash
curl -i http://localhost:8081/1
```

You should see `HTTP/1.1 302` with `Location: https://example.com`.

### Resetting the local database

The init scripts only run when the Postgres volume is first created. If you change them, or want a clean slate:

```bash
docker compose -f infra/docker-compose.yml down -v
docker compose -f infra/docker-compose.yml up -d
```

## Configuration

Every setting has a local-development default and can be overridden with an environment variable. **Override all credentials in production.**

### Create URL Service

| Variable | Default | Purpose |
|---|---|---|
| `PORT` | `8080` | HTTP port |
| `DB_URL` | `jdbc:postgresql://localhost:5432/urlshortener_db` | JDBC URL |
| `DB_OWNER_USERNAME` | `create_url_owner` | Read/write DB user |
| `DB_OWNER_PASSWORD` | `ownerpass` | Read/write DB password |
| `APP_BASE_URL` | `http://localhost:8081` | Public base URL used to build `shortUrl` in responses (should point at the redirect service) |

### Redirect Service

| Variable | Default | Purpose |
|---|---|---|
| `PORT` | `8081` | HTTP port |
| `DB_URL` | `jdbc:postgresql://localhost:5432/urlshortener_db` | JDBC URL |
| `DB_READER_USERNAME` | `redirect_reader` | Read-only DB user |
| `DB_READER_PASSWORD` | `readerpass` | Read-only DB password |
| `REDIS_URL` | `redis://localhost:6379` | Redis connection URL |

### Running behind a reverse proxy

The rate limiter identifies clients by IP. Behind a proxy or load balancer, make sure the proxy **overwrites** the `X-Forwarded-For` header (rather than appending to a client-supplied one) so callers can't spoof their IP and bypass the limit.

## Testing

### Unit and integration tests

Integration tests use Testcontainers to start throwaway PostgreSQL and Redis containers, so you only need Docker running:

```bash
cd create-url-service && ./mvnw test
cd ../redirect-service && ./mvnw test
```

### Rate-limit load test (k6)

With the create service running:

```bash
k6 run load-tests/k6/rate-limit.js
# Against another host:
k6 run -e BASE_URL=http://localhost:8080 load-tests/k6/rate-limit.js
```

The test sends requests past the 10/minute limit and asserts the first 10 return `201` and the rest return `429`. Wait a minute between runs so the bucket refills.

## Docker Images

Both Dockerfiles copy paths like `create-url-service/pom.xml`, so build from the **repository root**:

```bash
docker build -f create-url-service/Dockerfile -t url-shortener-create .
docker build -f redirect-service/Dockerfile   -t url-shortener-redirect .
```

## Tech Stack

| Technology | Purpose |
|---|---|
| Java 21 | Language |
| Spring Boot 4 | REST services |
| Spring Data JPA / Hibernate | Database access |
| PostgreSQL 16 | Persistent URL mappings |
| Redis 7 | Redirect caching |
| Bucket4j | Rate limiting |
| Maven | Build and dependency management |
| Docker / Docker Compose | Local infrastructure and containerization |
| JUnit 5, Mockito, Testcontainers | Testing |
| k6 | Load testing |

## Known Limitations

Deliberate trade-offs for v1:

- **Short codes are sequential**, so they are guessable and enumerable. Treat every short URL as public.
- **Rate limiting is in-memory and per instance.** Running multiple create-service instances multiplies the effective limit. A shared store (e.g. Redis-backed Bucket4j) would fix this.
- **No abuse protection** (malicious-link scanning, domain blocklists).
- **No authentication or user accounts**, and no click analytics.
- **No delete or edit endpoint** for created URLs.

## License

MIT, see [LICENSE](LICENSE).

Built as a backend and system-design project to explore **service separation, URL generation, caching, persistence, and scalability**.
