# Tenant Microservice — Testing Guide

**Service:** `tenant_service`  
**Base URL:** `http://localhost:8082`  
**Swagger UI:** `http://localhost:8082/swagger-ui.html`  
**OpenAPI JSON:** `http://localhost:8082/api-docs`

> **Note:** The tenant service does **not** issue JWT tokens. It validates tokens issued by `auth_service` (port 8081). You must log in via `auth_service` first and use the returned `accessToken` for all protected endpoints.

---

## Prerequisites

- MySQL running on `localhost:3306` with schema `myonline` (auto-created on first run)
- `auth_service` running on port **8081** (required to obtain JWT tokens)
- Java 17 installed
- Maven installed (or use `./mvnw`)

---

## 1. Start the Application

### Option A — Run locally with Maven
```bash
cd tenant_service
./mvnw spring-boot:run
```

### Option B — Run with Docker Compose (both services)
```bash
docker-compose up --build
```

The application starts on port **8082**.  
On startup, `data.sql` seeds the database with 5 default subscription packages.

---

## 2. Obtain a JWT Token (from auth_service)

Before testing protected endpoints, log in to `auth_service` to get an access token:

```
POST http://localhost:8081/api/auth/login
Content-Type: application/json

{
  "email": "superadmin@myonline.com",
  "password": "Admin@12345"
}
```

**Action:** Copy `accessToken` from the response → set Postman collection variable `token = <accessToken>`

> The Super Admin user carries the `TENANT_PAYMENT_APPROVAL` permission, which grants full access to all tenant service endpoints.

---

## 3. Default Seed Data

### Seed Subscription Packages

| Package Code | Package Name       | Product Range | Registration Fee | Monthly Fee |
|--------------|--------------------|---------------|-----------------|-------------|
| `STARTER`    | Starter Package    | 1 – 50        | 5,000           | 1,000       |
| `BASIC`      | Basic Package      | 51 – 100      | 8,000           | 1,500       |
| `STANDARD`   | Standard Package   | 101 – 250     | 12,000          | 2,500       |
| `PREMIUM`    | Premium Package    | 251 – 500     | 18,000          | 4,000       |
| `ENTERPRISE` | Enterprise Package | 501 – 1,000   | 25,000          | 6,000       |

> Packages are seeded using `INSERT IGNORE` — safe to run on every startup.

---

## 4. Postman Testing Guide

Import the following requests into Postman. Set environment variables:
- `base_url = http://localhost:8082`
- `token = <accessToken from auth_service login>`

---

### 4.1 Tenant Fees (Subscription Package Management)

> **Permission:** View/Search requires `TENANT_PAYMENT_VIEW` **or** `TENANT_PAYMENT_APPROVAL`.  
> Create/Update/Delete require `TENANT_PAYMENT_APPROVAL`.

---

#### Get All Packages
```
GET {{base_url}}/api/tenant-fees
Authorization: Bearer {{token}}
```
**Expected:** `200 OK` with all 5 seeded packages ordered by product count range ascending

---

#### Get Package by Code
```
GET {{base_url}}/api/tenant-fees/STARTER
Authorization: Bearer {{token}}
```
**Expected:** `200 OK` with the STARTER package details

---

#### Search Packages by Product Count Range
```
GET {{base_url}}/api/tenant-fees/search?from=1&to=100
Authorization: Bearer {{token}}
```
**Expected:** `200 OK` with STARTER and BASIC packages  
**Note:** Both `from` and `to` are optional. Omit either to remove that filter bound.

---

#### Create Package
```
POST {{base_url}}/api/tenant-fees
Authorization: Bearer {{token}}
Content-Type: application/json

{
  "packageCode": "ULTRA",
  "packageName": "Ultra Package",
  "productCountFrom": 1001,
  "productCountTo": 2000,
  "registrationFee": 35000,
  "monthlyFee": 9000
}
```
**Expected:** `201 Created` with the newly created package  
**Note:** Package code must be unique. Product count range must not overlap with existing packages.

---

#### Update Package (Partial)
```
PUT {{base_url}}/api/tenant-fees/ULTRA
Authorization: Bearer {{token}}
Content-Type: application/json

{
  "packageName": "Ultra Pro Package",
  "monthlyFee": 9500
}
```
**Expected:** `200 OK` with updated package  
**Note:** Only provided fields are updated. Package code cannot be changed.

---

#### Delete Package
```
DELETE {{base_url}}/api/tenant-fees/ULTRA
Authorization: Bearer {{token}}
```
**Expected:** `200 OK`  
**Note:** Use a non-seeded package code to avoid removing seed data.

---

### 4.2 Tenant Registration

> **Public endpoints:** Submit registration and check domain availability require **no authentication**.  
> **Protected endpoints:** List, search, view, and approve/reject require a valid Bearer token.

---

#### Check Domain Availability (Public)
```
GET {{base_url}}/api/tenant-registrations/check-domain?domainPrefix=myshop
```
**Expected:** `200 OK` with `available: true` and the checked domain prefix  
**Note:** No token required. A domain is unavailable if it exists in any registration request or active tenant.

```
GET {{base_url}}/api/tenant-registrations/check-domain?domainPrefix=taken-domain
```
**Expected:** `200 OK` with `available: false` (if that domain is already registered)

---

#### Submit Registration (Public)
```
POST {{base_url}}/api/tenant-registrations
Content-Type: application/json

{
  "tenantBusinessName": "My Online Shop",
  "domainPrefix": "myshop",
  "mailingAddress1": "123 Main Street, Dhaka",
  "mailingAddress2": "Block B, Floor 2",
  "contactPerson": "John Doe",
  "contactNumber1": "01712345678",
  "contactNumber2": "01812345678",
  "emailAddress": "owner@myshop.com",
  "maxInventoryItems": 50,
  "packageCode": "STARTER",
  "registrationFeePmtChannel": "bKash",
  "registrationFeePmtRef": "TXN123456",
  "registrationFeePmtReceiptBase64": null
}
```
**Expected:** `201 Created` with the submitted registration request  
**Note:** No token required. `registrationFee` and `monthlyPayment` are auto-populated from the selected package. `maxInventoryItems` must fall within the selected package's product count range. Domain prefix must be unique and contain only lowercase letters, digits, and hyphens.

---

#### Submit Registration — With Payment Receipt (Public)
```
POST {{base_url}}/api/tenant-registrations
Content-Type: application/json

{
  "tenantBusinessName": "Second Shop",
  "domainPrefix": "secondshop",
  "mailingAddress1": "456 Commerce Road, Chittagong",
  "contactPerson": "Jane Smith",
  "contactNumber1": "01987654321",
  "emailAddress": "jane@secondshop.com",
  "maxInventoryItems": 80,
  "packageCode": "BASIC",
  "registrationFeePmtChannel": "Nagad",
  "registrationFeePmtRef": "NGD789012",
  "registrationFeePmtReceiptBase64": "<base64-encoded-image-string>"
}
```
**Expected:** `201 Created`

---

#### Get All Registrations
```
GET {{base_url}}/api/tenant-registrations
Authorization: Bearer {{token}}
```
**Expected:** `200 OK` with all registrations ordered by submission date (newest first)

---

#### Get Registration by ID
```
GET {{base_url}}/api/tenant-registrations/1
Authorization: Bearer {{token}}
```
**Expected:** `200 OK` with full registration details

---

#### Search Registrations (with Filters)
```
GET {{base_url}}/api/tenant-registrations/search?approvalStatus=P
Authorization: Bearer {{token}}
```
**Expected:** `200 OK` with all Pending registrations

```
GET {{base_url}}/api/tenant-registrations/search?packageCode=STARTER&tenantBusinessName=shop
Authorization: Bearer {{token}}
```
**Expected:** `200 OK` — filtered by package and partial business name match (case-insensitive)

**Available filter parameters (all optional):**

| Parameter            | Type            | Description                                           | Example         |
|----------------------|-----------------|-------------------------------------------------------|-----------------|
| `id`                 | Long            | Filter by exact registration ID                       | `1`             |
| `packageCode`        | String          | Filter by exact package code                          | `STARTER`       |
| `tenantBusinessName` | String          | Partial, case-insensitive business name search        | `My Shop`       |
| `approvalStatus`     | P / A / R       | Filter by status (Pending / Approved / Rejected)      | `P`             |
| `domainPrefix`       | String          | Filter by exact domain prefix                         | `myshop`        |
| `contactNumber`      | String          | Searches both contactNumber1 and contactNumber2       | `01712345678`   |
| `emailAddress`       | String          | Filter by exact email address                         | `owner@myshop.com` |

---

#### Approve Registration
```
PUT {{base_url}}/api/tenant-registrations/1/decision
Authorization: Bearer {{token}}
Content-Type: application/json

{
  "approved": true,
  "remarks": "Payment verified. Approved."
}
```
**Expected:** `200 OK` with the updated registration and the newly created tenant info record  
**Note:** Only `Pending (P)` registrations can be processed. On approval, a `TenantInfo` record is automatically created with `Active (A)` status.

---

#### Reject Registration
```
PUT {{base_url}}/api/tenant-registrations/2/decision
Authorization: Bearer {{token}}
Content-Type: application/json

{
  "approved": false,
  "remarks": "Payment receipt could not be verified. Please resubmit."
}
```
**Expected:** `200 OK` with the updated registration showing `Rejected (R)` status  
**Note:** No tenant account is created on rejection.

---

## 5. Error Scenarios to Test

| Scenario                                                          | Expected HTTP Code |
|-------------------------------------------------------------------|--------------------|
| Access protected endpoint without token                           | `403 Forbidden`    |
| Access protected endpoint with expired token                      | `403 Forbidden`    |
| Access protected endpoint with insufficient permissions           | `403 Forbidden`    |
| Submit registration with duplicate domain prefix                  | `409 Conflict`     |
| Create package with duplicate package code                        | `409 Conflict`     |
| Create package with overlapping product count range               | `409 Conflict`     |
| Submit registration with `maxInventoryItems` outside package range| `400 Bad Request`  |
| Submit registration with invalid domain prefix (uppercase/spaces) | `400 Bad Request`  |
| Submit registration with missing required fields                  | `400 Bad Request`  |
| Create package with `productCountFrom >= productCountTo`          | `400 Bad Request`  |
| Get registration/package with non-existent ID or code            | `404 Not Found`    |
| Approve/Reject a registration that is already Approved or Rejected| `400 Bad Request`  |
| Submit registration with invalid email format                     | `400 Bad Request`  |

---

## 6. Swagger UI Testing

1. Open `http://localhost:8082/swagger-ui.html`
2. Click **Authorize** (top-right)
3. Log in to `auth_service` at `http://localhost:8081/api/auth/login` to get an access token
4. Enter: `Bearer <your_access_token>`
5. All protected endpoints are now testable directly from the browser
6. Public endpoints (`POST /api/tenant-registrations`, `GET /api/tenant-registrations/check-domain`) can be tested without authorization

---

## 7. Database Verification

Connect to MySQL (`localhost:3306`, user: `root`, password: `root`, schema: `myonline`) and run:

```sql
-- Verify tables were created
SHOW TABLES;

-- Check seeded subscription packages
SELECT package_code, package_name, product_count_from, product_count_to,
       registration_fee, monthly_fee
FROM tenant_fees
ORDER BY product_count_from;

-- Check submitted registration requests
SELECT id, tenant_business_name, domain_prefix, package_code,
       approval_status, created_at
FROM tenant_registration_request
ORDER BY created_at DESC;

-- Check approved tenant accounts
SELECT tenant_id, tenant_business_name, domain_prefix, package_code,
       status, created_at
FROM tenant_info;

-- Verify registration-to-tenant relationship on approval
SELECT r.id AS registration_id, r.tenant_business_name, r.approval_status,
       t.tenant_id, t.status AS tenant_status
FROM tenant_registration_request r
LEFT JOIN tenant_info t ON t.registration_request_id = r.id
ORDER BY r.id;
```

---

## 8. Notes

- **Token validation:** The tenant service validates JWT tokens issued by `auth_service`. Both services must share the same JWT secret (`app.jwt.secret` in `application.properties`).
- **Public endpoints:** `POST /api/tenant-registrations` and `GET /api/tenant-registrations/check-domain` do not require authentication and are accessible to anyone.
- **Fee auto-population:** When a registration is submitted, `registrationFee` and `monthlyPayment` are automatically copied from the selected `tenant_fees` package — these fields are not accepted from the client.
- **Domain uniqueness:** A domain prefix is checked against both `tenant_registration_request` and `tenant_info` tables to prevent conflicts at any stage.
- **Approval workflow:** Only `Pending (P)` registrations can be approved or rejected. Once processed, the status cannot be changed again.
- **Tenant creation on approval:** Approving a registration automatically creates a `TenantInfo` record with `Active (A)` status. The registration request ID is stored in `TenantInfo` for audit traceability.
- **Package range overlap:** Two subscription packages cannot have overlapping product count ranges. This is validated on both create and update.
- **Payment receipt:** The `registrationFeePmtReceiptBase64` field accepts a Base64-encoded image string. It is stored as a binary blob (`LONGBLOB`) in the database.
- All API responses follow a consistent `ApiResponse<T>` wrapper format.
