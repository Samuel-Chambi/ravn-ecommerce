# SportHub E-Commerce API

A production-grade sports e-commerce backend API built with **Spring Boot 3.5** and **Clean Architecture**, featuring a hybrid REST + GraphQL architecture, Stripe payments, JWT authentication, and comprehensive email notifications.

## Tech Stack

| Layer | Technology |
|---|---|
| Framework | Spring Boot 3.5.10 (Java 21) |
| Database | PostgreSQL + Hibernate ORM |
| Migrations | Flyway |
| Auth | JWT (JJWT 0.12.6) + Redis token blacklist |
| API | REST + GraphQL (Spring GraphQL) |
| Payments | Stripe (v25.1.0) |
| Storage | AWS S3 (product images) |
| Email | Resend API / SMTP + Thymeleaf templates |
| Rate Limiting | Redis-based (Redisson 3.25.2) |
| Build | Maven |

## Architecture

The project follows **Clean Architecture** principles, organizing code into four concentric layers with strict dependency rules — outer layers depend on inner layers, never the reverse.

```
┌──────────────────────────────────────────────────────────────────┐
│                      PRESENTATION LAYER                          │
│   REST Controllers · GraphQL Resolvers · Filters · Exception     │
│   Handlers · Request/Response serialization                      │
├──────────────────────────────────────────────────────────────────┤
│                      INFRASTRUCTURE LAYER                        │
│   JPA Persistence Adapters · Mappers · Email Providers (SMTP /   │
│   Resend) · AWS S3 Storage · Stripe · Redis · Spring Security    │
│   · Event Publishing                                             │
├──────────────────────────────────────────────────────────────────┤
│                      APPLICATION LAYER                           │
│   Use Cases · Commands & Queries · DTOs · Repository Interfaces  │
│   · Service Interfaces · Event Publisher Interface · Validation   │
├──────────────────────────────────────────────────────────────────┤
│                        DOMAIN LAYER                              │
│   Entities (Product, Order, User, Cart) · Value Objects ·        │
│   Domain Events · Domain Exceptions · Business Rules             │
└──────────────────────────────────────────────────────────────────┘
```

### Layer Responsibilities

**Domain Layer** — Pure business logic with zero framework dependencies. Contains entities (`Product`, `Order`, `User`, `Cart`), domain events (`OrderStatusChangedEvent`, `LowStockEvent`, `RefundRequestedEvent`), and a custom exception hierarchy rooted in `DomainException`.

**Application Layer** — Orchestrates use cases through a generic `UseCase<I, O>` interface. Defines repository and service contracts (ports) that the infrastructure layer implements. Houses command/query objects as immutable Java records and DTOs for data transformation.

**Infrastructure Layer** — Implements all ports defined by the application layer. Each domain repository has a corresponding JPA adapter (e.g., `ProductRepository` → `ProductRepositoryImpl`) backed by Spring Data JPA repositories and bidirectional mappers (`Mapper<JpaEntity, DomainModel>`). External integrations (Stripe, S3, email providers) live here.

**Presentation Layer** — Thin layer of REST controllers and GraphQL resolvers that accept validated requests, delegate to use cases, and return response DTOs. Includes a centralized `GlobalExceptionHandler` and HTTP filters for JWT auth and rate limiting.

## Design Patterns

| Pattern | Where It's Applied |
|---|---|
| **Repository** | 14 repository interfaces in the application layer with JPA adapter implementations in infrastructure. Decouples business logic from persistence. |
| **Strategy** | `EmailService` with two interchangeable implementations (`SmtpEmailServiceImpl`, `ResendEmailServiceImpl`) selected via `@ConditionalOnProperty`. Same pattern for `ImageStorageService` (`LocalStorageServiceImpl`, `S3StorageServiceImpl`). |
| **Observer / Event-Driven** | Domain events (`OrderStatusChangedEvent`, `LowStockEvent`, `ProductDiscountedEvent`, `RefundRequestedEvent`) published through an `EventPublisher` port, handled asynchronously by `NotificationEventListener` with `@Async` + `@EventListener`. |
| **Command** | Immutable Java records (`CreateProductCommand`, `UpdateDeliveryStatusCommand`, etc.) encapsulate use case inputs. Controllers build commands and pass them to the corresponding `UseCase.execute()`. |
| **Adapter** | Repository adapters bridge application-layer interfaces to Spring Data JPA. Bidirectional `Mapper<I, O>` implementations convert between JPA entities and domain models. |
| **Builder** | Lombok `@Builder` on domain entities, DTOs, and error responses for fluent, readable object construction. |
| **Facade** | Each use case acts as a facade, coordinating multiple repositories and services behind a single `execute()` method. `EmailTemplateEngine` wraps Thymeleaf complexity. |
| **Template Method** | The generic `UseCase<I, O>` interface defines a standardized execution contract that all use cases implement, enforcing a consistent invocation pattern across the application. |
| **Filter Chain** | `JwtAuthFilter` (extends `OncePerRequestFilter`) handles token extraction and validation. `RateLimitFilter` enforces request limits on password reset. Both participate in Spring's filter chain. |
| **DTO** | Separate request DTOs (with Bean Validation annotations) and response DTOs with static factory methods (`ProductResponse.toDto()`). Cleanly separates API contract from domain models. |
| **Mapper** | A `Mapper<I, O>` interface with implementations for each entity (`ProductMapper`, `OrderMapper`, etc.) provides bidirectional conversion between JPA entities and domain models. |
| **Singleton** | All Spring-managed beans (`@Service`, `@Component`, `@Repository`) default to singleton scope, ensuring a single shared instance per service. |

## Features

### Authentication & Authorization
- JWT-based stateless authentication with 24h token expiration
- BCrypt password hashing
- Token blacklist on logout via Redis
- Role-based access control: `CLIENT` and `MANAGER` roles
- Password reset flow with email token (rate-limited: 3 attempts/hour/IP)

### Product Management
- Full CRUD operations (MANAGER only for write operations)
- Cursor-based pagination, sorting, and category filtering
- Enable/disable products without deletion (soft delete)
- Product image upload to AWS S3
- Manager-only endpoint to view all products including disabled ones

### Shopping Cart (GraphQL)
- Per-user cart management
- Stock validation before adding items
- Automatic total calculation
- Add, update quantity, remove items, and clear cart

### Orders & Payments
- Create orders from cart contents
- Stripe PaymentIntent integration
- Webhook handler with signature verification and idempotent processing
- Order status flow: `PENDING` → `PAID` → `SHIPPED` → `DELIVERED`
- Cancel orders (client own orders or manager any order)
- Email notifications on status changes

### Refund & Return System
- Clients request refunds with a reason
- Manager approval/rejection workflow
- Approved refunds trigger Stripe refund + inventory restock
- Email notifications at each step (request, approval, rejection)

### Favorites (Likes)
- Like/unlike products
- Paginated list of favorite products
- Automatic notifications when liked products have low stock or price drops

### Reviews
- CRUD operations for product reviews
- Cursor-based pagination
- Users can only modify their own reviews

### Email Notifications
- Async event-driven email system
- Dual provider support: SMTP and Resend API
- Thymeleaf HTML templates: welcome, password reset, order status, low stock alerts, discount alerts, refund notifications

## API Overview

### REST Endpoints

| Method | Endpoint | Auth | Role |
|---|---|---|---|
| `POST` | `/auth/signup` | No | - |
| `POST` | `/auth/signin` | No | - |
| `POST` | `/auth/signout` | Yes | Any |
| `POST` | `/auth/forgot-password` | No | - |
| `POST` | `/auth/reset-password` | No | - |
| `GET` | `/products` | No | - |
| `GET` | `/products/{id}` | No | - |
| `POST` | `/products` | Yes | MANAGER |
| `PUT` | `/products/{id}` | Yes | MANAGER |
| `DELETE` | `/products/{id}` | Yes | MANAGER |
| `GET` | `/products/manage` | Yes | MANAGER |
| `GET` | `/products/{id}/reviews` | No | - |
| `POST` | `/products/{id}/reviews` | Yes | Any |
| `PUT` | `/products/{id}/reviews/{reviewId}` | Yes | Owner |
| `DELETE` | `/products/{id}/reviews/{reviewId}` | Yes | Owner |
| `POST` | `/payments/orders/{orderId}/intent` | Yes | CLIENT |
| `GET` | `/payments/orders/{orderId}` | Yes | Any |
| `POST` | `/payments/webhook` | No | - |
| `POST` | `/refunds` | Yes | CLIENT |
| `GET` | `/refunds/me` | Yes | CLIENT |
| `GET` | `/refunds/pending` | Yes | MANAGER |
| `POST` | `/refunds/{id}/approve` | Yes | MANAGER |
| `POST` | `/refunds/{id}/reject` | Yes | MANAGER |

### GraphQL Endpoint: `POST /graphql`

**Queries:**

```graphql
myOrders(first: Int, after: String): OrderConnection!
myOrderById(orderId: ID!): Order!
allOrders(first: Int, after: String): OrderConnection!        # MANAGER
orderById(orderId: ID!): Order!                                # MANAGER
myCart: Cart!
likedProducts(first: Int, after: String): ProductConnection!
```

**Mutations:**

```graphql
addToCart(input: AddToCartInput!): Cart!
updateCartItem(input: UpdateCartItemInput!): Cart!
removeFromCart(cartItemId: ID!): Cart!
clearCart: Boolean!
createOrderFromCart: Order!
cancelUserOrderById(orderId: ID!): Boolean!
deleteProduct(productId: ID!): Boolean!                        # MANAGER
disableProduct(productId: ID!): Product!                       # MANAGER
enableProduct(productId: ID!): Product!                        # MANAGER
updateDeliveryStatus(orderId: ID!, status: OrderStatus!): Order!  # MANAGER
cancelOrderById(orderId: ID!): Boolean!                        # MANAGER
likeProduct(productId: ID!): Boolean!
unlikeProduct(productId: ID!): Boolean!
```

GraphiQL playground is available at `/graphiql` when enabled.

## Getting Started

### Prerequisites

- Java 21
- Maven
- PostgreSQL
- Redis

### Environment Variables

| Variable | Description | Default |
|---|---|---|
| `DATABASE_URL` | PostgreSQL JDBC URL | - |
| `DATABASE_USERNAME` | Database username | - |
| `DATABASE_PASSWORD` | Database password | - |
| `JWT_SECRET` | JWT signing key (min 32 chars) | - |
| `REDIS_HOST` | Redis host | `localhost` |
| `REDIS_PORT` | Redis port | `6379` |
| `REDIS_USERNAME` | Redis username | `default` |
| `REDIS_PASSWORD` | Redis password | - |
| `STRIPE_API_KEY` | Stripe secret key | - |
| `STRIPE_WEBHOOK_SECRET` | Stripe webhook signing secret | - |
| `AWS_S3_BUCKET` | S3 bucket name | `ravn-ecommerce-product-images` |
| `AWS_REGION` | AWS region | `us-east-2` |
| `AWS_ACCESS_KEY_ID` | AWS access key | - |
| `AWS_SECRET_ACCESS_KEY` | AWS secret key | - |
| `EMAIL_PROVIDER` | Email provider (`smtp` or `resend`) | `smtp` |
| `EMAIL_FROM` | Sender email address | `noreply@socimep.org` |
| `EMAIL_HOST` | SMTP host | `smtp.gmail.com` |
| `EMAIL_PORT` | SMTP port | `587` |
| `EMAIL_USERNAME` | SMTP username | - |
| `EMAIL_PASSWORD` | SMTP password | - |
| `RESEND_API_KEY` | Resend API key (if using Resend) | - |
| `CORS_ALLOWED_ORIGINS` | Allowed CORS origins | `*` |
| `PORT` | Server port | `8080` |
| `RATE_LIMIT_REQUESTS` | Max password reset requests | `3` |
| `RATE_LIMIT_WINDOW` | Rate limit window in seconds | `3600` |

### Run Locally

```bash
# Clone the repository
git clone https://github.com/Samuel-Chambi/ravn-ecommerce.git
cd ravn-ecommerce

# Set environment variables (or create a .env file)
export DATABASE_URL=jdbc:postgresql://localhost:5432/ecommerce
export DATABASE_USERNAME=postgres
export DATABASE_PASSWORD=yourpassword
export JWT_SECRET=your-secret-key-must-be-at-least-32-characters-long
export REDIS_HOST=localhost

# Build and run
./mvnw spring-boot:run
```

The API will be available at `http://localhost:8080`.

## Railway Deployment

This project is configured to deploy on [Railway](https://railway.app).

### Step 1: Create a Railway Project

1. Go to [railway.app](https://railway.app) and create a new project.
2. Connect your GitHub repository (`ravn-ecommerce`).

### Step 2: Provision Services

Add the following services to your Railway project:

- **PostgreSQL** - Click "New" → "Database" → "PostgreSQL"
- **Redis** - Click "New" → "Database" → "Redis"

### Step 3: Configure Environment Variables

In the Railway dashboard, go to your app service and add the following variables under the **Variables** tab:

#### Database (use Railway reference variables)

```
DATABASE_URL=${{Postgres.JDBC_DATABASE_URL}}
DATABASE_USERNAME=${{Postgres.PGUSER}}
DATABASE_PASSWORD=${{Postgres.PGPASSWORD}}
```

> Railway provides reference variables from provisioned databases. Use `${{Postgres.JDBC_DATABASE_URL}}` to auto-link the PostgreSQL connection URL.

#### Redis (use Railway reference variables)

```
REDIS_HOST=${{Redis.REDISHOST}}
REDIS_PORT=${{Redis.REDISPORT}}
REDIS_USERNAME=${{Redis.REDISUSER}}
REDIS_PASSWORD=${{Redis.REDISPASSWORD}}
```

#### Application

```
JWT_SECRET=<generate-a-secure-random-string-min-32-chars>
PORT=8080
CORS_ALLOWED_ORIGINS=https://your-frontend-domain.com
```

#### Stripe

```
STRIPE_API_KEY=sk_live_...
STRIPE_WEBHOOK_SECRET=whsec_...
```

#### AWS S3

```
AWS_S3_BUCKET=your-bucket-name
AWS_REGION=us-east-2
AWS_ACCESS_KEY_ID=AKIA...
AWS_SECRET_ACCESS_KEY=...
```

#### Email

```
EMAIL_PROVIDER=resend
RESEND_API_KEY=re_...
EMAIL_FROM=noreply@yourdomain.com
```

Or for SMTP:

```
EMAIL_PROVIDER=smtp
EMAIL_HOST=smtp.gmail.com
EMAIL_PORT=587
EMAIL_USERNAME=your@gmail.com
EMAIL_PASSWORD=your-app-password
```

### Step 4: Configure Build Settings

In Railway's service **Settings** tab:

| Setting | Value |
|---|---|
| Builder | Nixpacks |
| Build Command | `./mvnw -DskipTests package` |
| Start Command | `java -jar target/ecommerce-0.0.1-SNAPSHOT.jar` |

> Railway uses [Nixpacks](https://nixpacks.com) by default, which auto-detects Java/Maven projects. The build command skips tests during deployment since the database is not available at build time.

### Step 5: Deploy

1. Push to your repository's `main` branch. Railway auto-deploys on every push.
2. Monitor the deployment logs in the Railway dashboard.
3. Once deployed, Railway provides a public URL (e.g., `https://your-app.up.railway.app`).

### Step 6: Configure Stripe Webhook

After deployment, register your Railway URL as a Stripe webhook endpoint:

1. Go to [Stripe Dashboard](https://dashboard.stripe.com/webhooks) → Webhooks.
2. Add endpoint: `https://your-app.up.railway.app/payments/webhook`.
3. Select events: `payment_intent.succeeded`, `payment_intent.payment_failed`.
4. Copy the webhook signing secret and set it as `STRIPE_WEBHOOK_SECRET` in Railway.

### Troubleshooting Railway

| Issue | Solution |
|---|---|
| Build fails | Check that Java 21 is detected. Add `NIXPACKS_JDK_VERSION=21` if needed. |
| Database connection error | Verify `DATABASE_URL` uses the `${{Postgres.JDBC_DATABASE_URL}}` reference variable. |
| Redis connection refused | Ensure Redis service is provisioned and `REDIS_HOST`/`REDIS_PORT` reference variables are set. |
| Port binding error | Ensure `PORT` is set to `8080` and `server.port` reads from `${PORT}`. |
| Flyway migration error | Flyway runs on startup. Check that the database is accessible before the app starts. |

## Project Structure

```
src/main/java/com/ravn/ecommerce/
│
├── domain/                          # DOMAIN LAYER (innermost)
│   ├── model/
│   │   ├── product/                 # Product, Inventory, Category, Like, ProductImage
│   │   ├── order/                   # Order, OrderItem, OrderStatus, Refund
│   │   ├── cart/                    # Cart, CartItem, CartStatus
│   │   ├── user/                    # User, UserRole, Address
│   │   └── payment/                 # Payment, PaymentStatus, StripeWebhookEvent
│   └── exceptions/                  # DomainException hierarchy
│
├── application/                     # APPLICATION LAYER
│   ├── usecases/                    # UseCase<I,O> implementations
│   │   ├── product/command/         # CreateProductCommand, UpdateProductCommand
│   │   ├── order/command/           # UpdateDeliveryStatusCommand
│   │   ├── auth/                    # SignUpUseCase, SignInUseCase
│   │   └── ...
│   ├── repositories/                # Repository interfaces (ports)
│   ├── services/                    # EmailService, ImageStorageService interfaces
│   ├── dto/request/                 # Inbound DTOs with validation
│   ├── dto/response/                # Outbound DTOs with factory methods
│   ├── events/                      # EventPublisher interface
│   └── config/                      # AppConfig (externalized properties)
│
├── infrastructure/                  # INFRASTRUCTURE LAYER
│   ├── persistence/
│   │   ├── adapters/                # 14 repository implementations
│   │   └── jpa/
│   │       ├── entity/              # JPA-annotated entities
│   │       ├── repository/          # Spring Data JPA repositories
│   │       └── mapper/              # Bidirectional Mapper<JpaEntity, Domain>
│   ├── email/                       # SmtpEmailServiceImpl, ResendEmailServiceImpl
│   ├── external/storage/            # LocalStorageServiceImpl, S3StorageServiceImpl
│   ├── security/                    # JwtService, JwtAuthFilter, TokenBlacklistService
│   ├── events/                      # SpringEventPublisher, NotificationEventListener
│   ├── stripe/                      # Stripe payment integration
│   ├── ratelimit/                   # Redis-based rate limiting
│   └── config/                      # SecurityConfig, CorsConfig, S3ClientConfig
│
├── presentation/                    # PRESENTATION LAYER (outermost)
│   ├── rest/controllers/            # 13 REST controllers
│   ├── rest/exceptions/             # GlobalExceptionHandler, ErrorResponse
│   ├── filter/                      # RateLimitFilter
│   └── graphql/                     # GraphQL resolvers and exception handling
│
src/main/resources/
├── application.yml                  # Application configuration
├── graphql/schema.graphqls          # GraphQL schema definition
└── templates/                       # Thymeleaf email templates
```

## License

This project was built as part of the RAVN Development challenge.
