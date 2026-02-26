# Refund Feature – Implementation Plan v2

## Objective

Fix the compilation bugs and import inconsistencies left from the initial scaffolding of the Refund feature.
All domain models, use cases, persistence adapters, and the REST controller already exist.
The changes below are **bug fixes only** — no new files need to be created.

---

## Current State

| Component | Status |
|---|---|
| `Refund.java` (domain model) | ✅ Done |
| `RefundStatus.java` (enum) | ✅ Done |
| `OrderStatus.REFUNDED` | ✅ Done |
| `Order.markAsRefunded()`, `canRequestRefund()` | ✅ Done |
| `Order.cancel()` limited to PENDING only | ✅ Done |
| `RefundRepository` (interface) | ✅ Done |
| `RefundJpaEntity.java` | ⚠️ Bug – `status` field is `String` instead of `RefundStatus` |
| `RefundMapper.java` | ⚠️ Will auto-fix once `RefundJpaEntity` is corrected |
| `RefundJpaRepository.java` | ✅ Done |
| `RefundRepositoryImpl.java` | ✅ Done |
| `RequestRefundUseCase.java` | ⚠️ Bug – wrong `OrderRepository` import |
| `ApproveRefundUseCase.java` | ⚠️ Bug – wrong imports for `EventPublisher`, `OrderStatusChangedEvent`, and all repositories |
| `RejectRefundUseCase.java` | ⚠️ Unused import of `EventPublisher` (won't block compilation, but should be cleaned) |
| `GetPendingRefundsUseCase.java` | ✅ Done |
| `GetUserRefundsUseCase.java` | ✅ Done |
| `CancelOrderUseCase.java` | ⚠️ Bug – references `command.orderId()` but method receives `Long orderId` |
| `CancelUserOrderUseCase.java` | ✅ Done |
| `RefundRequest.java` (DTO) | ✅ Done |
| `ReviewRefund.java` (DTO) | ✅ Done |
| `RefundController.java` | ✅ Done |
| `SecurityConfig.java` | ⚠️ Bug – paths use `/v1/refunds/**` but controller is mapped to `/refunds` |

---

## Proposed Changes

### 1. `RefundJpaEntity.java`
**Bug:** The `status` field is declared as `String` but annotated with `@Enumerated(EnumType.STRING)`, causing a type mismatch with the `RefundStatus` enum.

**Fix:** Change field type from `String` to `RefundStatus`.

```diff
- @Enumerated(EnumType.STRING)
- @Column(nullable = false, length = 20)
- private String status;
+ @Enumerated(EnumType.STRING)
+ @Column(nullable = false, length = 20)
+ private RefundStatus status;
```

---

### 2. `ApproveRefundUseCase.java`
**Bug:** Uses wrong package paths for `EventPublisher`, `OrderStatusChangedEvent`, and all repository interfaces.

**Fix:** Replace all wrong imports:

```diff
- import com.ravn.ecommerce.domain.events.publisher.EventPublisher;
- import com.ravn.ecommerce.domain.events.types.OrderStatusChangedEvent;
- import com.ravn.ecommerce.domain.repository.OrderRepository;
- import com.ravn.ecommerce.domain.repository.PaymentRepository;
- import com.ravn.ecommerce.domain.repository.ProductRepository;
- import com.ravn.ecommerce.domain.repository.UserRepository;
+ import com.ravn.ecommerce.application.events.EventPublisher;
+ import com.ravn.ecommerce.domain.model.order.events.OrderStatusChangedEvent;
+ import com.ravn.ecommerce.application.repositories.OrderRepository;
+ import com.ravn.ecommerce.application.repositories.PaymentRepository;
+ import com.ravn.ecommerce.application.repositories.ProductRepository;
+ import com.ravn.ecommerce.application.repositories.UserRepository;
```

---

### 3. `RequestRefundUseCase.java`
**Bug:** Uses `domain.repository.OrderRepository` instead of `application.repositories.OrderRepository`. Also imports `EventPublisher` which is never used.

**Fix:**

```diff
- import com.ravn.ecommerce.domain.events.publisher.EventPublisher;
- import com.ravn.ecommerce.domain.repository.OrderRepository;
+ import com.ravn.ecommerce.application.repositories.OrderRepository;
```

---

### 4. `RejectRefundUseCase.java`
**Cleanup:** Remove the commented-out unused `EventPublisher` import.

```diff
- import com.ravn.ecommerce.domain.events.publisher.EventPublisher;
```

---

### 5. `CancelOrderUseCase.java`
**Bug:** Compilation error on line 46 — `command` is undefined. The method signature is `execute(Long orderId)`, so `command.orderId()` should be `orderId`.

**Fix:**

```diff
- throw new InvalidOrderException(
-         String.format(
-                 "Order ID %d cannot be cancelled instantly (current status: %s). ...",
-                 command.orderId(),
-                 order.getStatus()));
+ throw new InvalidOrderException(
+         String.format(
+                 "Order ID %d cannot be cancelled instantly (current status: %s). ...",
+                 orderId,
+                 order.getStatus()));
```

---

### 6. `SecurityConfig.java`
**Bug:** Refund endpoint matchers use `/v1/refunds/**` path prefix, but the `RefundController` is mapped to `/refunds`.

**Fix:**

```diff
- .requestMatchers(HttpMethod.POST, "/v1/refunds").hasRole("CLIENT")
- .requestMatchers(HttpMethod.GET, "/v1/refunds/me").hasRole("CLIENT")
- .requestMatchers(HttpMethod.GET, "/v1/refunds/pending").hasRole("MANAGER")
- .requestMatchers(HttpMethod.POST, "/v1/refunds/{refundId}/approve").hasRole("MANAGER")
- .requestMatchers(HttpMethod.POST, "/v1/refunds/{refundId}/reject").hasRole("MANAGER")
+ .requestMatchers(HttpMethod.POST, "/refunds").hasRole("CLIENT")
+ .requestMatchers(HttpMethod.GET, "/refunds/me").hasRole("CLIENT")
+ .requestMatchers(HttpMethod.GET, "/refunds/pending").hasRole("MANAGER")
+ .requestMatchers(HttpMethod.POST, "/refunds/{refundId}/approve").hasRole("MANAGER")
+ .requestMatchers(HttpMethod.POST, "/refunds/{refundId}/reject").hasRole("MANAGER")
```

---

## Verification Plan

### 1. Compilation Check
```bash
mvn compile -q
```
Must complete with **BUILD SUCCESS**.

### 2. Unit Tests
```bash
mvn test -q
```
All existing tests must pass.

### 3. Manual API Flow
With the application running:

1. `POST /auth/login` (CLIENT) → get JWT
2. Create an order and have it paid (via Stripe webhook or test fixture)
3. `POST /refunds` `{"orderId": <id>, "reason": "Damaged item"}` → expect `201 Created`, `status: PENDING`
4. `GET /refunds/me` → refund appears in list
5. `POST /auth/login` (MANAGER) → get Manager JWT
6. `GET /refunds/pending` → refund appears in list
7. `POST /refunds/{id}/approve` `{"adminNote": "Approved"}` → expect `200 OK`
8. Verify: Order status = `REFUNDED`, inventory restocked, Stripe refund issued
