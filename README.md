# Shining English API

Spring Boot REST API cho hệ thống Shining English — học tiếng Anh online.

---

## Kiến Trúc Tổng Quan

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                           INTERNET / NGƯỜI DÙNG                            │
└────────────────────────┬────────────────────────────────────────────────────┘
                         │
                         ▼
┌────────────────────────────────────────────────────────────────────────────┐
│                            CLOUD LOAD BALANCER                             │
│                         (HAProxy / Nginx / AWS ALB)                        │
└────┬──────────────────────┬──────────────────────┬─────────────────────────┘
     │                      │                      │
     ▼                      ▼                      ▼
┌──────────┐        ┌──────────┐            ┌──────────┐
│  NextJS  │        │  NextJS  │    ...      │  NextJS  │  ← Frontend
│  (FE #1) │        │  (FE #2) │            │  (FE #N) │     (2+ replicas)
└────┬─────┘        └────┬─────┘            └────┬─────┘
     │                   │                       │
     │  /api/proxy/*     │                       │
     └─────────┬─────────┘                       │
               │                                 │
        ┌──────┴──────┐                          │
        │  INTERNAL    │                          │
        │  NETWORK     │                          │
        └──────┬──────┘                          │
               │                                  │
               ▼                                  │
┌──────────────────────────────┐                  │
│     API GATEWAY / REVERSE    │                  │
│         PROXY (Nginx)        │                  │
│                              │                  │
│  /api/v1/*  →  Spring Boot   │                  │
│  /cms/*     →  Laravel PHP   │                  │
│  /storage/* →  MinIO / S3    │                  │
└────┬──────────────────┬──────┘                  │
     │                  │                         │
     ▼                  ▼                         │
┌──────────┐     ┌──────────┐                     │
│ Spring   │     │ Laravel  │                     │
│ Boot API │     │ PHP CMS  │                     │
│ (2+ rep) │     │ (1+ rep) │                     │
└────┬─────┘     └────┬─────┘                     │
     │                │                           │
     ▼                ▼                           │
┌──────────────────────────────────────┐          │
│           MySQL 8.4 (Primary)        │          │
│    ┌───────────────────────────┐     │          │
│    │  shining_english          │     │          │
│    │  ┌─ courses              │     │          │
│    │  ├─ lessons              │     │          │
│    │  ├─ orders               │     │          │
│    │  ├─ users                │     │          │
│    │  └─ ... (35+ tables)     │     │          │
│    └───────────────────────────┘     │          │
│                                      │          │
│    MySQL 8.4 (Replica / Read-Only)   │          │
└──────────────────────────────────────┘          │
                         │                        │
                         ▼                        │
┌──────────────────────────────────────┐          │
│  Object Storage (MinIO / S3)         │          │
│  ┌─ uploads/avatars/*               │          │
│  ├─ uploads/courses/*               │          │
│  ├─ uploads/blogs/*                 │          │
│  └─ uploads/documents/*             │          │
└──────────────────────────────────────┘          │
                                                  │
┌──────────────────────────────────────┐          │
│  Cache / Queue (Redis)               │          │
│  ┌─ Session                         │          │
│  ├─ Rate Limiting                   │          │
│  └─ Queue Jobs (async email, etc.)  │          │
└──────────────────────────────────────┘          │
```

---

## Luồng Gọi API

### 1. Luồng Request Từ Frontend

```
┌────────┐     ┌────────┐     ┌──────────┐     ┌──────────┐
│  User  │────▶│ NextJS │────▶│  Nginx   │────▶│  Spring  │
│Browser │◀────│  FE    │◀────│  Proxy   │◀────│   Boot   │
└────────┘     └────────┘     └──────────┘     └──────────┘
                    │                                │
                    │  Gọi backend qua                │
                    │  /api/proxy/...                 │
                    │  + attach access token          │
                    │  + attach user token (cookie)   │
                    │                                │
                    │  Server-side render             │
                    │  (SSR) cho SEO                  │
```

**Chi tiết proxy flow (NextJS → Spring Boot):**

```
Browser                          NextJS                              Spring Boot
  │                                │                                   │
  │  GET /courses                  │                                   │
  │───────────────────────────────▶│                                   │
  │                                │                                   │
  │                                │  POST /api/v1/access-token        │
  │                                │  (if no cached developer token)   │
  │                                │──────────────────────────────────▶│
  │                                │  { access_token: "dev-xxx" }     │
  │                                │◀──────────────────────────────────│
  │                                │                                   │
  │                                │  GET /api/v1/courses              │
  │                                │  Authorization: Bearer dev-xxx    │
  │                                │  User-Authorization: user-xxx     │
  │                                │──────────────────────────────────▶│
  │                                │  [courses list]                   │
  │                                │◀──────────────────────────────────│
  │  SSR HTML                      │                                   │
  │◀───────────────────────────────│                                   │
```

### 2. Luồng Authentication

```
─── Đăng ký / Đăng nhập ─────────────────────────────────────────

  ┌──────┐     ┌──────────┐     ┌──────────┐     ┌────────┐
  │ User │────▶│  NextJS  │────▶│  Spring  │────▶│MySQL DB│
  │      │◀────│   Proxy  │◀────│   Boot   │◀────│        │
  └──────┘     └──────────┘     └──────────┘     └────────┘
  POST /auth/register              │
  POST /auth/login                 ├─ BCrypt password → users
  POST /auth/third-party-login     ├─ SHA-256(token) → personal_access_tokens
                                   ├─ UUID token → response
                                   ├─ Dispatch UserRegisteredEvent
                                   │   ├─ InitUserStarListener (async)
                                   │   │   └─ StarService.addStarByUserId(15)
                                   │   └─ SendEmailVerificationListener (async)
                                   │       └─ Log only (mail chưa config)
                                   └─ LoginResponse { token, user }

─── Xác thực request ─────────────────────────────────────────────

  Request Headers:
    User-Authorization: <plain_text_token>    → UserTokenFilter
    Authorization: Bearer <developer_token>    → DeveloperTokenFilter

  UserTokenFilter (chạy trước):
    ├─ SHA-256(token)
    ├─ Tìm trong personal_access_tokens WHERE name = 'user_auth_token'
    └─ Nếu tồn tại → SecurityContextHolder.set(user)

  DeveloperTokenFilter (chạy sau, kiểm tra nếu chưa có user auth):
    ├─ Lấy từ Authorization header
    ├─ SHA-256(token)
    ├─ Tìm WHERE name = 'developer_access_token'
    └─ Nếu tồn tại → SecurityContextHolder.set(developer)

  Endpoint nhận Authentication từ Spring Security Context.
```

### 3. Luồng Thanh Toán COD

```
User                                           Spring Boot                     Admin (CMS)
 │                                                │                              │
 │  POST /orders  {"type":"cart","payment_method":"cod"}
 │───────────────────────────────────────────────▶│                              │
 │                                                │                              │
 │  createOrderRecord(total > 0, COD):            │                              │
 │  - status → OrderStatus.Pending                │                              │
 │  - paid_at → null                              │                              │
 │                                                │                              │
 │  DB::afterCommit:                              │                              │
 │  - enrollmentService.enroll(user, course, order)│                             │
 │    + Tạo enrollment record                     │                              │
 │    + isEnrolled → false (vì order chưa paid)   │                              │
 │                                                │                              │
 │  { order_id: 123, status: "pending" }          │                              │
 │◀──────────────────────────────────────────────│                              │
 │                                                │                              │
 │  GET /courses/1/access                         │                              │
 │───────────────────────────────────────────────▶│                              │
 │                                                │                              │
 │  isEnrolled → false                            │                              │
 │  hasPendingEnrollment → true                   │                              │
 │  { enrolled: false, pending_access: true }     │                              │
 │◀──────────────────────────────────────────────│                              │
 │                                                │                              │
 │                                                │  Admin confirm payment       │
 │                                                │◀─────────────────────────────│
 │                                                │                              │
 │                                                │  UPDATE orders               │
 │                                                │  SET status = 'paid',        │
 │                                                │      paid_at = NOW()         │
 │                                                │                              │
 │  GET /courses/1/access                         │                              │
 │───────────────────────────────────────────────▶│                              │
 │  isEnrolled → true (order.paid = true)         │                              │
 │  { enrolled: true, pending_access: false }     │                              │
 │◀──────────────────────────────────────────────│                              │
```

---

## Module Structure (Spring Boot)

```
shiningenglishapi/
├── controller/
│   └── v1/
│       ├── user/          AuthController, UserController, HomeController
│       ├── course/        CourseController
│       ├── lesson/        LessonController, LessonNoteController
│       ├── blog/          BlogController
│       ├── cart/          CartController
│       ├── transaction/   OrderController, PaymentWebhookController
│       ├── quiz/          QuizAttemptController
│       ├── city/          CityController
│       ├── contact/       ContactController
│       ├── dashboard/     DashboardController
│       ├── developer/     DeveloperController
│       └── star/          StarController
│
├── service/
│   ├── user/              UserService
│   ├── course/            CourseService, CourseReviewService
│   ├── lesson/            LessonService, LessonAccessService, ...
│   ├── cart/              CartService
│   ├── order/             OrderService
│   ├── star/              StarService
│   ├── quiz/              UserQuizAttemptService
│   ├── developer/         DeveloperService
│   ├── dashboard/         DashboardService
│   └── enrollment/        EnrollmentService
│
├── repository/
│   ├── user/              UserRepository, UserDeviceRepository, ...
│   ├── course/            CourseRepository, CourseReviewRepository
│   ├── lesson/            LessonRepository, LessonProgressRepository, ...
│   ├── quiz/              QuizRepository, QuizQuestionRepository, ...
│   ├── cart/              CartRepository
│   ├── order/             OrderRepository, OrderItemRepository
│   ├── star/              StarRepository, StarTransactionRepository
│   └── blog/              BlogRepository, BlogTagRepository
│
├── event/                 UserRegisteredEvent, LessonCompletedEvent, ...
│   └── listener/          InitUserStarListener, GrantLessonStarRewardListener, ...
│
├── security/              UserTokenFilter, DeveloperTokenFilter, RecaptchaVerifier
├── config/                SecurityConfig, WebConfig, AsyncConfig
├── common/                BaseController, BaseService, GlobalExceptionHandler
├── model/
│   ├── entity/            User, Course, Lesson, Order, Star, ...
│   └── dto/               LoginResponse, RegisterResponse
├── enums/                 OrderStatus, PaymentMethod, AuthenticatedBy, ...
├── valueobject/           QueryOption, CourseFilter, DeviceInfo, ...
└── util/                  UrlBuilder
```

---

## Deploy

### Development (Docker Compose)

```yaml
services:
  app:          # Spring Boot với hot-reload (port 8080)
  mysql:        # MySQL 8.4 (port 3306)
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
      replicas: 2                # Spring Boot replica
      update_config:
        parallelism: 1
        order: start-first       # Zero-downtime deploy
      healthcheck:
        test: ["CMD", "curl", "-f", "http://localhost:8080/up"]
        interval: 10s
        start_period: 30s
```

**CI/CD Pipeline (đề xuất):**

```
Git Push → GitHub Actions / GitLab CI
  └─ ./gradlew build -x test
  └─ docker build -f docker/prod/Dockerfile -t ${IMAGE}
  └─ docker push ${IMAGE}
  └─ docker stack deploy -c docker/prod/docker-stack.yml
```

### Environment Variables

| Variable | Mặc định | Mô tả |
|---|---|---|
| `MYSQL_HOST` | `localhost` | MySQL host |
| `MYSQL_PORT` | `3306` | MySQL port |
| `MYSQL_DATABASE` | `shining_english` | Database name |
| `MYSQL_USER` | `shining` | DB user |
| `MYSQL_PASSWORD` | - | DB password |
| `JPA_DDL_AUTO` | `validate` | Hibernate DDL mode |
| `JPA_SHOW_SQL` | `false` | Log SQL queries |
| `APP_URL` | `http://localhost:8000` | App base URL (cho thumbnail) |
| `STAR_REGISTRATION_BONUS` | `15` | Star thưởng đăng ký |
| `STAR_DAILY_CHECKIN` | `1` | Star thưởng check-in |
| `STAR_COURSE_COMPLETE` | `10` | Star thưởng hoàn thành khóa |
| `RECAPTCHA_SECRET` | (empty) | Google reCAPTCHA secret |

---

## API Response Format

Tất cả response đều theo format thống nhất:

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

- Thành công: `status: true`, `status_code` tương ứng HTTP
- Lỗi: `status: false`, `message` mô tả lỗi
- Pagination: `meta` object trong response list
- Field names: **snake_case** (Jackson globally configured)

---

## Công Nghệ

| Thành phần | Công nghệ |
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
