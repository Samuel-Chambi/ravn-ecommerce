# Refund Feature – Implementation Plan v3

## Goal

Apply the corrections identified after v2 to the Refund feature:

1. **DB / Persistence**: Refund statuses must be stored as `int` in the database (same as `orders.status`), and the mapper must manually convert between the `int` and the `RefundStatus` enum.
2. **Manual Mapper**: `RefundMapper` must be reimplemented as a manual `@Component` class with switch expressions, following the same pattern as `OrderMapper` and `PaymentMapper`.
3. **Response DTO**: Create `RefundResponse` and use it in the controller and use cases, avoiding direct exposure of the domain entity.
4. **Cancellation / Refund business logic**:
   - If the order is `PENDING` and gets cancelled → restore inventory + set status to `CANCELLED`.
   - If the order is in any other eligible state → generate the `Refund` (request). The admin then approves or rejects it. If approved → `REFUNDED`. If rejected → remains in its original state.
5. **OrderMapper**: Missing `REFUNDED` (6) case in the conversions.

---

## Proposed Changes

---

### Persistence – JPA Entity & Repository

#### [MODIFY] [RefundJpaEntity.java](file:///d:/RAVN/ravn-ecommerce/src/main/java/com/ravn/ecommerce/infrastructure/persistence/jpa/entity/RefundJpaEntity.java)

Changed the `status` field from `@Enumerated(EnumType.STRING) RefundStatus` to `int` (same as `OrderJpaEntity`).

```diff
- import com.ravn.ecommerce.domain.model.order.RefundStatus;
- import jakarta.persistence.EnumType;
- import jakarta.persistence.Enumerated;
  ...
- @Enumerated(EnumType.STRING)
- @Column(nullable = false, length = 20)
- private RefundStatus status;
+ @Column(nullable = false)
+ private int status;
```

#### [MODIFY] [RefundJpaRepository.java](file:///d:/RAVN/ravn-ecommerce/src/main/java/com/ravn/ecommerce/infrastructure/persistence/jpa/repository/RefundJpaRepository.java)

The `findByStatus` method now accepts `int` instead of `String`.

```diff
- List<RefundJpaEntity> findByStatus(String status);
+ List<RefundJpaEntity> findByStatus(int status);
```

---

### Mapper Layer

#### [MODIFY] [RefundMapper.java](file:///d:/RAVN/ravn-ecommerce/src/main/java/com/ravn/ecommerce/infrastructure/persistence/jpa/mapper/RefundMapper.java)

Replaced the MapStruct interface with a manual `@Component` class using switch expressions, following the `PaymentMapper` pattern.

**Mapping (based on the comment in `RefundStatus.java`):**
- `1` → `PENDING`
- `2` → `APPROVED`
- `3` → `REJECTED`

```java
@Component
/*
 * RefundStatus mapping:
 * 1 -> PENDING
 * 2 -> APPROVED
 * 3 -> REJECTED
 */
public class RefundMapper implements Mapper<RefundJpaEntity, Refund> {

    @Override
    public Refund toDomain(RefundJpaEntity entity) {
        return Refund.builder()
                .id(entity.getId())
                .orderId(entity.getOrderId())
                .userId(entity.getUserId())
                .reason(entity.getReason())
                .adminNote(entity.getAdminNote())
                .status(switch (entity.getStatus()) {
                    case 2 -> RefundStatus.APPROVED;
                    case 3 -> RefundStatus.REJECTED;
                    default -> RefundStatus.PENDING;
                })
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    @Override
    public RefundJpaEntity toJpaEntity(Refund domain) {
        return RefundJpaEntity.builder()
                .id(domain.getId())
                .orderId(domain.getOrderId())
                .userId(domain.getUserId())
                .reason(domain.getReason())
                .adminNote(domain.getAdminNote())
                .status(switch (domain.getStatus()) {
                    case APPROVED -> 2;
                    case REJECTED -> 3;
                    default -> 1;
                })
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }
}
```

#### [MODIFY] [OrderMapper.java](file:///d:/RAVN/ravn-ecommerce/src/main/java/com/ravn/ecommerce/infrastructure/persistence/jpa/mapper/OrderMapper.java)

Added the missing `REFUNDED` (6) case in both conversions.

```diff
  case 4 -> OrderStatus.CANCELLED;
  case 5 -> OrderStatus.SHIPPED;
+ case 6 -> OrderStatus.REFUNDED;
  default -> OrderStatus.PENDING;
```

```diff
  case OrderStatus.CANCELLED -> 4;
  case OrderStatus.SHIPPED -> 5;
+ case OrderStatus.REFUNDED -> 6;
  default -> 1;
```

---

### Persistence Adapter

#### [MODIFY] [RefundRepositoryImpl.java](file:///d:/RAVN/ravn-ecommerce/src/main/java/com/ravn/ecommerce/infrastructure/persistence/adapters/RefundRepositoryImpl.java)

- Changed `mapper.toEntity(refund)` to `mapper.toJpaEntity(refund)` (to match the `Mapper<I,O>` interface method).
- In `findByStatus`, convert `RefundStatus` to `int` before calling the repository.

```diff
  public Refund save(Refund refund) {
-     RefundJpaEntity entity = mapper.toEntity(refund);
+     RefundJpaEntity entity = mapper.toJpaEntity(refund);
  ...

  public List<Refund> findByStatus(RefundStatus status) {
-     return repository.findByStatus(status.name()).stream()
+     int statusInt = switch (status) {
+         case APPROVED -> 2;
+         case REJECTED -> 3;
+         default -> 1;
+     };
+     return repository.findByStatus(statusInt).stream()
```

---

### DTO Layer

#### [NEW] [RefundResponse.java](file:///d:/RAVN/ravn-ecommerce/src/main/java/com/ravn/ecommerce/application/dto/response/RefundResponse.java)

Created a response DTO following the `OrderResponse` pattern with a static `toDto(Refund)` method.

```java
package com.ravn.ecommerce.application.dto.response;

import com.ravn.ecommerce.domain.model.order.Refund;
import com.ravn.ecommerce.domain.model.order.RefundStatus;
import lombok.*;
import java.time.LocalDateTime;

@Data @Builder @AllArgsConstructor @NoArgsConstructor
public class RefundResponse {
    private Long id;
    private Long orderId;
    private Long userId;
    private String reason;
    private String adminNote;
    private RefundStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static RefundResponse toDto(Refund refund) {
        return RefundResponse.builder()
                .id(refund.getId())
                .orderId(refund.getOrderId())
                .userId(refund.getUserId())
                .reason(refund.getReason())
                .adminNote(refund.getAdminNote())
                .status(refund.getStatus())
                .createdAt(refund.getCreatedAt())
                .updatedAt(refund.getUpdatedAt())
                .build();
    }
}
```

---

### Use Cases

#### [MODIFY] [RequestRefundUseCase.java](file:///d:/RAVN/ravn-ecommerce/src/main/java/com/ravn/ecommerce/application/usecases/refund/RequestRefundUseCase.java)

Changed the return type from `Refund` to `RefundResponse`.

```diff
- public class RequestRefundUseCase implements UseCase<RequestRefundCommand, Refund> {
+ public class RequestRefundUseCase implements UseCase<RequestRefundCommand, RefundResponse> {
  ...
-     Refund savedRequest = refundRepository.save(newRequest);
-     return savedRequest;
+     Refund savedRequest = refundRepository.save(newRequest);
+     return RefundResponse.toDto(savedRequest);
```

#### [MODIFY] [GetUserRefundsUseCase.java](file:///d:/RAVN/ravn-ecommerce/src/main/java/com/ravn/ecommerce/application/usecases/refund/GetUserRefundsUseCase.java)

Changed the return type from `List<Refund>` to `List<RefundResponse>`.

#### [MODIFY] [GetPendingRefundsUseCase.java](file:///d:/RAVN/ravn-ecommerce/src/main/java/com/ravn/ecommerce/application/usecases/refund/GetPendingRefundsUseCase.java)

Changed the return type from `List<Refund>` to `List<RefundResponse>`.

---

### Controller

#### [MODIFY] [RefundController.java](file:///d:/RAVN/ravn-ecommerce/src/main/java/com/ravn/ecommerce/presentation/rest/controllers/RefundController.java)

Replaced all `Refund` return types with `RefundResponse`.

```diff
- import com.ravn.ecommerce.domain.model.order.Refund;
+ import com.ravn.ecommerce.application.dto.response.RefundResponse;
  ...
- public ResponseEntity<Refund> requestRefund(...)
+ public ResponseEntity<RefundResponse> requestRefund(...)
  ...
- public ResponseEntity<List<Refund>> getMyRefunds()
+ public ResponseEntity<List<RefundResponse>> getMyRefunds()
  ...
- public ResponseEntity<List<Refund>> getPendingRefunds()
+ public ResponseEntity<List<RefundResponse>> getPendingRefunds()
```

---

## Business Logic Review

The cancellation/refund logic is already correctly defined:

| Scenario | What happens |
|---|---|
| Order in `PENDING` + cancelled | `Order.cancel()` → status = `CANCELLED`, items restocked |
| Order in another state (`PAID`, `SHIPPED`, etc.) | **Cannot** be cancelled directly. Uses the refund flow: `RequestRefundUseCase` creates the request as `PENDING` |
| Admin approves refund | Stripe refund, inventory restocked, order → `REFUNDED`, payment → `REFUNDED`, refund → `APPROVED` |
| Admin rejects refund | Refund → `REJECTED`, order remains in its original state |

This logic **already exists and is correct** in `CancelOrderUseCase`, `CancelUserOrderUseCase`, `ApproveRefundUseCase`, and `RejectRefundUseCase`. No additional changes are needed in the use cases beyond the return type.

---

## Verification Plan

### Automated Tests

```bash
# 1. Verify compilation
mvn compile -q

# 2. Run all existing tests
mvn test -q
```

### Manual API Verification

With the application running (`mvn spring-boot:run`):

1. `POST /auth/login` (CLIENT user) → get JWT
2. With JWT: `POST /orders` → create order (status `PENDING`)
3. `DELETE /orders/{id}` (or cancel endpoint) → order becomes `CANCELLED`, inventory restored
4. Create another order, simulate payment (Stripe webhook or test fixture)
5. `POST /refunds` `{"orderId": <id>, "reason": "Damaged"}` → response `201`, body is `RefundResponse` with `status: PENDING`
6. `GET /refunds/me` → list of `RefundResponse`
7. `POST /auth/login` (MANAGER) → get manager JWT
8. `GET /refunds/pending` → see refund in list
9. `POST /refunds/{id}/approve` `{"adminNote": "OK"}` → `200 OK`
10. Verify in DB: `orders.status = 6` (REFUNDED), `refunds.status = 2` (APPROVED)
