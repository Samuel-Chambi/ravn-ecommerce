# E-Commerce Refund API Specification

The following endpoints manage the Refund logic of the platform.

## 1. Request a Refund (User)
Allows an authenticated user to request a refund for an order that is already `PAID`, `SHIPPED`, or `DELIVERED`.

**Endpoint:** `POST /refunds`

**Headers:**
- `Authorization`: Bearer `<CLIENT_JWT_TOKEN>`

**Request Body:**
```json
{
  "orderId": 123,
  "reason": "The item arrived defective."
}
```

**Response (201 Created):**
```json
{
  "id": 45,
  "orderId": 123,
  "userId": 10,
  "status": "PENDING",
  "reason": "The item arrived defective.",
  "createdAt": "2026-02-25T20:00:00Z"
}
```


## 2. Get User Refunds (User)
Fetches all refund requests created by the authenticated user.

**Endpoint:** `GET /refunds/me`

**Headers:**
- `Authorization`: Bearer `<CLIENT_JWT_TOKEN>`

**Response (200 OK):**
```json
[
  {
    "id": 45,
    "orderId": 123,
    "status": "PENDING",
    "createdAt": "2026-02-25T20:00:00Z"
  }
]
```


## 3. Get All Pending Refunds (Manager)
Fetches all refund requests that are waiting for approval or rejection.

**Endpoint:** `GET /refunds/pending`

**Headers:**
- `Authorization`: Bearer `<MANAGER_JWT_TOKEN>`

**Response (200 OK):**
```json
[
  {
    "id": 45,
    "orderId": 123,
    "userId": 10,
    "reason": "The item arrived defective.",
    "status": "PENDING",
    "createdAt": "2026-02-25T20:00:00Z"
  }
]
```


## 4. Approve Refund (Manager)
Approves a pending refund request. This automatically triggers a Refund via the Stripe API, updates the order/payment status, and restocks the inventory.

**Endpoint:** `POST /refunds/{refundId}/approve`

**Headers:**
- `Authorization`: Bearer `<MANAGER_JWT_TOKEN>`

**Response (200 OK):**
```json
{
  "message": "Refund approved successfully. Stripe refund processed and inventory restocked."
}
```


## 5. Reject Refund (Manager)
Rejects a pending refund request, adding a reason from the manager.

**Endpoint:** `POST /refunds/{refundId}/reject`

**Headers:**
- `Authorization`: Bearer `<MANAGER_JWT_TOKEN>`

**Request Body:**
```json
{
  "adminNote": "The refund window of 30 days has expired."
}
```

**Response (200 OK):**
```json
{
  "message": "Refund rejected."
}
```
