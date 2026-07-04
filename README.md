# Shining English API

Spring Boot REST API cho hệ thống Shining English — học tiếng Anh online.

---

## Kiến Trúc Tổng Quan

```mermaid
flowchart TB
    subgraph INTERNET["Internet / Người Dùng"]
        User([Browser User])
    end

    subgraph LB["Cloud Load Balancer"]
        ALB[HAProxy / Nginx / AWS ALB]
    end

    subgraph FE["Frontend - NextJS"]
        FE1["NextJS Replica #1"]
        FE2["NextJS Replica #2"]
        FEN["NextJS Replica #N"]
    end

    subgraph GW["API Gateway / Reverse Proxy (Nginx)"]
        direction TB
        SPR["/api/v1/* → Spring Boot"]
        CMS["/cms/* → Laravel PHP"]
        STG["/storage/* → MinIO / S3"]
    end

    subgraph BE["Backend Services"]
        SB[("Spring Boot API<br/>(2+ replicas)")]
        LARAVEL[("Laravel PHP CMS<br/>(1+ replica)")]
    end

    subgraph DB["Database Layer"]
        MYSQL_PRIMARY[("MySQL 8.4<br/>Primary")]
        MYSQL_REPLICA[("MySQL 8.4<br/>Replica / Read-Only)")]
    end

    subgraph STORAGE["Object Storage"]
        MINIO[("MinIO / S3")]
    end

    subgraph CACHE["Cache / Queue"]
        REDIS[("Redis")]
    end

    User --> ALB
    ALB --> FE1 & FE2 & FEN
    FE1 & FE2 & FEN -- /api/proxy/* --> GW
    GW --> SPR --> SB
    GW --> CMS --> LARAVEL
    GW --> STG --> MINIO
    SB & LARAVEL --> MYSQL_PRIMARY
    SB --> MYSQL_REPLICA
    SB & LARAVEL --> MINIO
    SB & LARAVEL --> REDIS
    MYSQL_PRIMARY -.-> MYSQL_REPLICA
```

---

## Kiến Trúc Module (Spring Boot)

```mermaid
flowchart LR
    subgraph Controller["controller/v1/"]
        U["user/<br/>Auth, User, Home"]
        C["course/<br/>Course"]
        L["lesson/<br/>Lesson, Note"]
        B["blog/<br/>Blog"]
        CA["cart/<br/>Cart"]
        T["transaction/<br/>Order, Payment"]
        Q["quiz/<br/>Attempt"]
        CI["city/<br/>City"]
        CO["contact/<br/>Contact"]
        D["dashboard/<br/>Dashboard"]
        DE["developer/<br/>Developer"]
        S["star/<br/>Star"]
    end

    subgraph Service["service/"]
        SU["user/"]
        SC["course/"]
        SL["lesson/"]
        SCA["cart/"]
        SO["order/"]
        SST["star/"]
        SQ["quiz/"]
        SD["dashboard/"]
        SDE["developer/"]
        SE["enrollment/"]
    end

    subgraph Repository["repository/"]
        RU["user/"]
        RC["course/"]
        RL["lesson/"]
        RQ["quiz/"]
        RCA["cart/"]
        RO["order/"]
        RS["star/"]
        RB["blog/"]
    end

    U & C & L & B & CA & T & Q & CI & CO & D & DE & S --> Service
    Service --> Repository
    Repository --> DB[(MySQL)]
```

---

## Luồng Gọi API

### 1. Request từ Frontend (Proxy Flow)

```mermaid
sequenceDiagram
    participant Browser as Browser User
    participant NextJS as NextJS (FE)
    participant Spring as Spring Boot API

    Browser->>NextJS: GET /courses
    Note over NextJS: Check cached developer access token

    alt No cached token
        NextJS->>Spring: POST /api/v1/access-token<br/>{email, password}
        Spring-->>NextJS: { access_token: "dev-xxx" }
        Note over NextJS: Cache token
    end

    NextJS->>Spring: GET /api/v1/courses<br/>Authorization: Bearer dev-xxx<br/>User-Authorization: user-xxx (from cookie)
    Spring-->>NextJS: [courses list]
    NextJS-->>Browser: SSR HTML
```

### 2. Authentication Flow

```mermaid
sequenceDiagram
    participant User as User
    participant NextJS as NextJS
    participant Spring as Spring Boot
    participant DB as MySQL

    User->>NextJS: POST /auth/register<br/>or /auth/login
    NextJS->>Spring: Forward request
    Spring->>DB: BCrypt(password) → users
    Spring->>DB: SHA-256(token) → personal_access_tokens
    Spring-->>NextJS: LoginResponse { token, user }

    par Async Events
        Spring->>Spring: Dispatch UserRegisteredEvent
        Spring-->>Spring: InitUserStarListener<br/>StarService.addStarByUserId(15)
        Spring-->>Spring: SendEmailVerificationListener<br/>(log only)
    end

    NextJS-->>User: Response + cookie token
```

### 3. Token Verification (Filter Chain)

```mermaid
flowchart LR
    REQ["Request"] --> UTF["UserTokenFilter<br/>User-Authorization header"]

    UTF -->|"SHA-256(token)<br/>→ personal_access_tokens<br/>WHERE name = 'user_auth_token'"| UTF_OK["Set User auth<br/>in SecurityContext"]

    UTF -->|"Token not found"| DTF["DeveloperTokenFilter<br/>Authorization header"]

    DTF -->|"SHA-256(token)<br/>→ personal_access_tokens<br/>WHERE name = 'developer_access_token'"| DTF_OK["Set Developer auth<br/>in SecurityContext<br/>(skip if User auth exists)"]
    DTF -->|"No valid token"| ANON["AnonymousAuthenticationFilter"]

    UTF_OK --> EP["Endpoint"]
    DTF_OK --> EP
    ANON --> EP
```

### 4. Thanh Toán COD

```mermaid
sequenceDiagram
    participant User as User
    participant Spring as Spring Boot
    participant Admin as Admin (CMS)

    User->>Spring: POST /orders { type:"cart", payment_method:"cod" }
    Spring->>Spring: createOrderRecord(total > 0, COD)
    Note right of Spring: status → OrderStatus.Pending<br/>paid_at → null
    Spring->>Spring: enrollmentService.enroll(user, course, order)
    Note over Spring: isEnrolled() → false<br/>(vì order chưa paid)
    Spring-->>User: { order_id: 123, status: "pending" }

    User->>Spring: GET /courses/1/access
    Spring-->>User: { enrolled: false, pending_access: true }

    Admin->>Spring: Admin confirm payment
    Spring->>Spring: UPDATE orders SET status='paid', paid_at=NOW()

    User->>Spring: GET /courses/1/access
    Spring-->>User: { enrolled: true, pending_access: false }
```

---

## Deploy

### Development (Docker Compose)

```yaml
services:
  app:          # Spring Boot với hot-reload (port 8080)
  mysql:        # MySQL 8.4
```

```bash
docker compose up -d
docker compose logs -f app
```

### Production (Docker Swarm)

```yaml
# docker/prod/docker-stack.yml
services:
  app:
    image: ${DOCKER_REGISTRY}/${APP_NAME}:${APP_TAG}
    deploy:
      replicas: 2
      update_config:
        parallelism: 1
        order: start-first
      healthcheck:
        test: ["CMD", "curl", "-f", "http://localhost:8080/up"]
```

**CI/CD Pipeline:**

```mermaid
flowchart LR
    GIT[Git Push] --> BUILD["./gradlew build -x test"]
    BUILD --> DOCKER["docker build -f docker/prod/Dockerfile"]
    DOCKER --> PUSH["docker push ${IMAGE}"]
    PUSH --> DEPLOY["docker stack deploy -c docker/prod/docker-stack.yml"]
```

### Environment Variables

| Variable | Default | Description |
|---|---|---|
| `MYSQL_HOST` | `localhost` | MySQL host |
| `MYSQL_PORT` | `3306` | MySQL port |
| `MYSQL_DATABASE` | `shining_english` | Database name |
| `MYSQL_USER` | `shining` | DB user |
| `MYSQL_PASSWORD` | - | DB password |
| `JPA_DDL_AUTO` | `validate` | Hibernate DDL mode |
| `JPA_SHOW_SQL` | `false` | Log SQL queries |
| `APP_URL` | `http://localhost:8000` | App base URL (thumbnail) |
| `STAR_REGISTRATION_BONUS` | `15` | Star thưởng đăng ký |
| `STAR_DAILY_CHECKIN` | `1` | Star thưởng check-in |
| `STAR_COURSE_COMPLETE` | `10` | Star thưởng hoàn thành khóa |
| `RECAPTCHA_SECRET` | (empty) | Google reCAPTCHA secret |

---

## API Response Format

```json
{
  "message": "OK",
  "status": true,
  "status_code": 200,
  "data": { ... },
  "meta": {
    "page": 1,
    "per_page": 15,
    "total": 100,
    "page_count": 7
  }
}
```

- Success: `status: true`, HTTP status code
- Error: `status: false`, `message` mô tả lỗi
- Pagination: `meta` object trong response list
- Field names: **snake_case** (Jackson globally configured)

---

## Công Nghệ

| Component | Technology |
|---|---|
| Runtime | Java 21 (Temurin) |
| Framework | Spring Boot 4.1.0 / Spring Security 7.x |
| ORM | Spring Data JPA + Hibernate |
| Database | MySQL 8.4 |
| Migration | Flyway |
| API Doc | Springdoc OpenAPI 3.x (`/swagger-ui.html`) |
| Cache | (optional) Redis |
| Object Storage | MinIO / S3-compatible |
| Build | Gradle 9.x |
| Deploy | Docker + Docker Swarm |
| Monitoring | Health endpoint `GET /up` |
