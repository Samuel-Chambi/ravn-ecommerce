# Refund Feature – Implementation Plan v3

## Objetivo

Aplicar las correcciones necesarias al feature de Refund identificadas después del v2:

1. **DB / Persistencia**: Los estados del Refund deben guardarse como `int` en la base de datos (igual que `orders.status`), y el mapper debe convertir entre el `int` y el enum `RefundStatus` manualmente.
2. **Mapper corregido**: `RefundMapper` debe reimplementarse como `@Component` manual con switch expressions, igual que `OrderMapper` y `PaymentMapper`.
3. **DTO de respuesta**: Crear `RefundResponse` y usarlo en el controller y use cases, evitando exponer la entity de dominio directamente.
4. **Lógica de negocio de cancelación / refund**:
   - Si la orden está en `PENDING` y se cancela → restaurar inventario + pasar a `CANCELLED`.
   - Si la orden está en cualquier otro estado elegible → generar el `Refund` (request). El admin luego aprueba o rechaza. Si se aprueba → `REFUNDED`. Si se rechaza → queda en su estado original.
5. **OrderMapper**: Le faltaba el case `REFUNDED` (6) en las conversiones.

---

## Proposed Changes

---

### Persistence – JPA Entity & Repository

#### [MODIFY] [RefundJpaEntity.java](file:///d:/RAVN/ravn-ecommerce/src/main/java/com/ravn/ecommerce/infrastructure/persistence/jpa/entity/RefundJpaEntity.java)

Cambiar el campo `status` de `@Enumerated(EnumType.STRING) RefundStatus` a `int` (igual que `OrderJpaEntity`).

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

El método `findByStatus` debe aceptar `int` en vez de `String`.

```diff
- List<RefundJpaEntity> findByStatus(String status);
+ List<RefundJpaEntity> findByStatus(int status);
```

---

### Mapper Layer

#### [MODIFY] [RefundMapper.java](file:///d:/RAVN/ravn-ecommerce/src/main/java/com/ravn/ecommerce/infrastructure/persistence/jpa/mapper/RefundMapper.java)

Reemplazar la interfaz MapStruct con una clase `@Component` manual que use switch expressions, siguiendo el patrón de `PaymentMapper`.

**Mapping (en base al comentario en `RefundStatus.java`):**
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

Agregar el case `REFUNDED` (6) que falta en ambas conversiones.

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

- Cambiar la llamada `mapper.toEntity(refund)` por `mapper.toJpaEntity(refund)` (para que coincida con el método de la interfaz `Mapper<I,O>`).
- En `findByStatus`, convertir `RefundStatus` a `int` antes de llamar al repo.

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

Crear un DTO de respuesta que siga el patrón de `OrderResponse` con un método estático `toDto(Refund)`.

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

#### [MODIFY] [RequestRefundUseCase.java](file:///d:/RAVN/ravn-ecommerce/src/main/java/com/ravn/ecommerce/application/usecases/order/RequestRefundUseCase.java)

Cambiar el tipo de retorno de `Refund` a `RefundResponse`.

```diff
- public class RequestRefundUseCase implements UseCase<RequestRefundCommand, Refund> {
+ public class RequestRefundUseCase implements UseCase<RequestRefundCommand, RefundResponse> {
  ...
-     Refund savedRequest = refundRepository.save(newRequest);
-     return savedRequest;
+     Refund savedRequest = refundRepository.save(newRequest);
+     return RefundResponse.toDto(savedRequest);
```

#### [MODIFY] [GetUserRefundsUseCase.java](file:///d:/RAVN/ravn-ecommerce/src/main/java/com/ravn/ecommerce/application/usecases/order/GetUserRefundsUseCase.java)

Cambiar el tipo de retorno de `List<Refund>` a `List<RefundResponse>`.

#### [MODIFY] [GetPendingRefundsUseCase.java](file:///d:/RAVN/ravn-ecommerce/src/main/java/com/ravn/ecommerce/application/usecases/order/GetPendingRefundsUseCase.java)

Cambiar el tipo de retorno de `List<Refund>` a `List<RefundResponse>`.

---

### Controller

#### [MODIFY] [RefundController.java](file:///d:/RAVN/ravn-ecommerce/src/main/java/com/ravn/ecommerce/presentation/rest/controllers/RefundController.java)

Reemplazar todos los `Refund` en los tipos de retorno por `RefundResponse`.

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

La lógica de cancellation/refund ya está correctamente definida:

| Escenario | Qué pasa |
|---|---|
| Orden en `PENDING` + se cancela | `Order.cancel()` → status = `CANCELLED`, items restockeados |
| Orden en otro estado (`PAID`, `SHIPPED`, etc.) | **No** se puede cancelar directamente. Se usa el flujo de refund: `RequestRefundUseCase` crea el request en `PENDING` |
| Admin aprueba el refund | Stripe refund, inventario restockeado, order → `REFUNDED`, payment → `REFUNDED`, refund → `APPROVED` |
| Admin rechaza el refund | Refund → `REJECTED`, orden permanece en su estado original |

Esta lógica **ya existe y es correcta** en `CancelOrderUseCase`, `CancelUserOrderUseCase`, `ApproveRefundUseCase`, y `RejectRefundUseCase`. No requiere cambios adicionales en las use cases más allá del tipo de retorno.

---

## Verification Plan

### Automated Tests

```bash
# 1. Verificar compilación
mvn compile -q

# 2. Ejecutar todos los tests existentes
mvn test -q
```

### Manual API Verification

Con la aplicación corriendo (`mvn spring-boot:run`):

1. `POST /auth/login` (usuario CLIENT) → obtener JWT
2. Con JWT: `POST /orders` → crear orden (estado `PENDING`)
3. `DELETE /orders/{id}` (o endpoint de cancel) → orden pasa a `CANCELLED`, inventario restaurado
4. Crear otra orden, simular pago (webhook Stripe o fixture de test)
5. `POST /refunds` `{"orderId": <id>, "reason": "Damaged"}` → respuesta `201`, body es `RefundResponse` con `status: PENDING`
6. `GET /refunds/me` → lista de `RefundResponse`
7. `POST /auth/login` (MANAGER) → obtener JWT de manager
8. `GET /refunds/pending` → ver refund en lista
9. `POST /refunds/{id}/approve` `{"adminNote": "OK"}` → `200 OK`
10. Verificar en DB: `orders.status = 6` (REFUNDED), `refunds.status = 2` (APPROVED)
