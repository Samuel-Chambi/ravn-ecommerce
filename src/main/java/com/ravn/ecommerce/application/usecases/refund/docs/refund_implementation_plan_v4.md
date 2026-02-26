# Refund Feature – Implementation Plan v4

## Goal

Corrections and enhancements to the Refund feature applied on top of v3:

1. **Persistence**: Refund statuses are stored as `int` in the DB (consistent with `orders.status`), and the mapper handles conversion between `int` and the `RefundStatus` enum.
2. **Manual Mapper**: `RefundMapper` was reimplemented as a `@Component` class with switch expressions (same convention as `OrderMapper` and `PaymentMapper`).
3. **`RefundResponse` DTO**: New response DTO to avoid exposing the domain entity in the API.
4. **`OrderMapper`**: Added the missing `REFUNDED` (6) case.
5. **Email Notification**: When a refund request is created, a `RefundRequestedEvent` is published that sends emails to all users with the MANAGER role.

---

## Proposed Changes

---

### Persistence – JPA Entity & Repository

#### [MODIFY] [RefundJpaEntity.java](file:///d:/RAVN/ravn-ecommerce/src/main/java/com/ravn/ecommerce/infrastructure/persistence/jpa/entity/RefundJpaEntity.java)

Changed the `status` field from `@Enumerated(EnumType.STRING) RefundStatus` to `int`.

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

`findByStatus` now accepts `int` instead of `String`.

```diff
- List<RefundJpaEntity> findByStatus(String status);
+ List<RefundJpaEntity> findByStatus(int status);
```

---

### Mapper Layer

#### [MODIFY] [RefundMapper.java](file:///d:/RAVN/ravn-ecommerce/src/main/java/com/ravn/ecommerce/infrastructure/persistence/jpa/mapper/RefundMapper.java)

Replaced the MapStruct `@Mapper` interface with a manual `@Component` class.

**Mapping:**
- `1` → `PENDING`
- `2` → `APPROVED`
- `3` → `REJECTED`

```java
@Component
public class RefundMapper implements Mapper<RefundJpaEntity, Refund> {
    // toDomain: switch (entity.getStatus()) { case 2 -> APPROVED; case 3 -> REJECTED; default -> PENDING; }
    // toJpaEntity: switch (domain.getStatus()) { case APPROVED -> 2; case REJECTED -> 3; default -> 1; }
}
```

#### [MODIFY] [OrderMapper.java](file:///d:/RAVN/ravn-ecommerce/src/main/java/com/ravn/ecommerce/infrastructure/persistence/jpa/mapper/OrderMapper.java)

Added the missing `REFUNDED` (6) case in both conversions.

```diff
  case 5 -> OrderStatus.SHIPPED;
+ case 6 -> OrderStatus.REFUNDED;
  default -> OrderStatus.PENDING;
```

```diff
  case OrderStatus.SHIPPED -> 5;
+ case OrderStatus.REFUNDED -> 6;
  default -> 1;
```

---

### Persistence Adapter

#### [MODIFY] [RefundRepositoryImpl.java](file:///d:/RAVN/ravn-ecommerce/src/main/java/com/ravn/ecommerce/infrastructure/persistence/adapters/RefundRepositoryImpl.java)

- `mapper.toEntity()` → `mapper.toJpaEntity()` (matches the `Mapper<I,O>` interface)
- `findByStatus`: converts `RefundStatus` to `int` before calling the JPA repository.

---

### DTO Layer

#### [NEW] [RefundResponse.java](file:///d:/RAVN/ravn-ecommerce/src/main/java/com/ravn/ecommerce/application/dto/response/RefundResponse.java)

Response DTO with a static `toDto(Refund)` method following the `OrderResponse` pattern.

Fields: `id`, `orderId`, `userId`, `reason`, `adminNote`, `status`, `createdAt`, `updatedAt`.

---

### Use Cases – Return Type Updates

#### [MODIFY] [RequestRefundUseCase.java](file:///d:/RAVN/ravn-ecommerce/src/main/java/com/ravn/ecommerce/application/usecases/refund/RequestRefundUseCase.java)

- Return type: `Refund` → `RefundResponse`
- Injected `EventPublisher` and publishes `RefundRequestedEvent` after saving.

#### [MODIFY] [GetUserRefundsUseCase.java](file:///d:/RAVN/ravn-ecommerce/src/main/java/com/ravn/ecommerce/application/usecases/refund/GetUserRefundsUseCase.java)

- Return type: `List<Refund>` → `List<RefundResponse>`

#### [MODIFY] [GetPendingRefundsUseCase.java](file:///d:/RAVN/ravn-ecommerce/src/main/java/com/ravn/ecommerce/application/usecases/refund/GetPendingRefundsUseCase.java)

- Return type: `List<Refund>` → `List<RefundResponse>`

---

### Controller

#### [MODIFY] [RefundController.java](file:///d:/RAVN/ravn-ecommerce/src/main/java/com/ravn/ecommerce/presentation/rest/controllers/RefundController.java)

All return types now use `RefundResponse` instead of `Refund`.

---

### Email Notification to Managers

#### [NEW] [RefundRequestedEvent.java](file:///d:/RAVN/ravn-ecommerce/src/main/java/com/ravn/ecommerce/domain/model/order/events/RefundRequestedEvent.java)

Domain event with: `refundId`, `orderId`, `userId`, `reason`, `occurredOn`.

#### [MODIFY] [UserRepository.java](file:///d:/RAVN/ravn-ecommerce/src/main/java/com/ravn/ecommerce/application/repositories/UserRepository.java)

Added `List<User> findByRole(UserRole role)`.

#### [MODIFY] [UserJpaRepository.java](file:///d:/RAVN/ravn-ecommerce/src/main/java/com/ravn/ecommerce/infrastructure/persistence/jpa/repository/UserJpaRepository.java)

Added `List<UserJpaEntity> findByRole(int role)`.

#### [MODIFY] [UserRepositoryImpl.java](file:///d:/RAVN/ravn-ecommerce/src/main/java/com/ravn/ecommerce/infrastructure/persistence/adapters/UserRepositoryImpl.java)

Implemented `findByRole`: converts `UserRole` enum → int (MANAGER=2, CLIENT=1) and delegates to the JPA repository.

#### [NEW] [refund-requested.html](file:///d:/RAVN/ravn-ecommerce/src/main/resources/templates/email/refund-requested.html)

Thymeleaf template that notifies the manager with refund details: ID, Order ID, User ID, and reason.

#### [MODIFY] [NotificationEventListener.java](file:///d:/RAVN/ravn-ecommerce/src/main/java/com/ravn/ecommerce/infrastructure/events/NotificationEventListener.java)

Added `onRefundRequested(RefundRequestedEvent)`:
- `@Async @EventListener @Transactional(readOnly = true)`
- Queries all users with `UserRole.MANAGER`
- Sends the `refund-requested` email to each one

Also added `"REFUNDED" -> "Refunded"` to `formatStatus()`.

---

## Business Logic Summary

| Scenario | Outcome |
|---|---|
| Order `PENDING` + cancel | `CANCELLED`, items restocked |
| Order `PAID`/`SHIPPED`/`DELIVERED` + cancel | Rejected — must use refund request |
| User requests refund | `Refund(PENDING)` created, email sent to all MANAGERs |
| Admin approves refund | Stripe refund, payment → `REFUNDED`, order → `REFUNDED`, inventory restocked |
| Admin rejects refund | Refund → `REJECTED`, order remains in its original state |

---

## Verification

### Compilation
```bash
mvn compile -q
# ✅ BUILD SUCCESS (exit code 0)
```

### Existing Tests
```bash
mvn test -q
```
