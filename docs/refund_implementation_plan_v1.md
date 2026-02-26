# Plan: Implement Refund System

## Objective
Enable users (CLIENT) to request refunds for PAID, SHIPPED, or DELIVERED orders, and allow Managers (MANAGER) to review, approve, and automatically process the monetary refund via Stripe while restoring product inventory.

## Proposed Changes

### 1. Domain Entities
- **Modify `OrderStatus.java`**: Add the `REFUNDED` state.
- **Add `RefundStatus.java`**: Enum containing `PENDING`, `APPROVED`, `REJECTED`.
- **Add `RefundRequest.java`**: Core domain model.
- **Add Repository Interfaces**: `RefundRequestRepository`.

### 2. Application Layer (Use Cases)
*   **Modify `CancelUserOrderUseCase` / `CancelOrderUseCase`**: Orders with `PAID` status can no longer be directly cancelled to trigger instant refunds. They must go through the formal `RefundRequest` workflow requiring MANAGER approval.
*   **`RequestRefundUseCase`**: Creates a PENDING `RefundRequest` if the order belongs to the user and is PAID, SHIPPED, or DELIVERED.
*   **`ApproveRefundUseCase`**: MANAGER only. Finds the `RefundRequest`, marks it as APPROVED, triggers `StripeService.refundPayment()`, changes the `Order` status to `REFUNDED`, and restocks the products.
*   **`RejectRefundUseCase`**: MANAGER only. Marks the `RefundRequest` as REJECTED.
*   **`GetRefundsUseCases`**: Queries for Clients to see their own requests, and for Managers to view all PENDING requests.

### 3. Persistence (Infrastructure)
*   **JPA Entity**: `RefundRequestJpaEntity`.
*   **Mapper**: `RefundRequestMapper`.
*   **Adapter**: `RefundRequestRepositoryImpl`.

### 4. Controllers (Presentation)
*   **`RefundController.java`**: 
    - `POST /refunds` (Client)
    - `GET /refunds/me` (Client)
    - `GET /refunds/pending` (Manager)
    - `POST /refunds/{refundId}/approve` (Manager)
    - `POST /refunds/{refundId}/reject` (Manager)
*   **Update `SecurityConfig.java`**: Secure the endpoints based on the required roles.

### 5. Documentation
*   Updated `docs/refund_architecture.md` and `docs/refund_api.md` with the new naming convention.

## Verification Plan
1. Create an order and mark it as PAID and DELIVERED.
2. Issue a request to the `POST /refunds` endpoint.
3. Assert that a PENDING `RefundRequest` is created.
4. Using a MANAGER JWT, hit the `/approve` endpoint.
5. Verify 3 outcomes: Stripe successfully processed the monetary refund, stock inventory increased, and the Order status changed to REFUNDED.
