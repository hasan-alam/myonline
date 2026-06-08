# Authorization Microservice — Testing Guide

**Service:** `auth_service`  
**Base URL:** `http://localhost:8081`  
**Swagger UI:** `http://localhost:8081/swagger-ui.html`  
**OpenAPI JSON:** `http://localhost:8081/api-docs`

---

## Prerequisites

- MySQL running on `localhost:3306` with schema `myonline` (auto-created on first run)
- Java 17 installed
- Maven installed (or use `./mvnw`)

---

## 1. Start the Application

### Option A — Run locally with Maven
```bash
cd auth_service
./mvnw spring-boot:run
```

### Option B — Run with Docker Compose
```bash
cd auth_service
docker-compose up --build
```

The application starts on port **8081**.  
On startup, `data.sql` seeds the database with default permissions, roles, and a Super Admin user.

---

## 2. Default Seed Data

### Default Super Admin User
| Field    | Value                         |
|----------|-------------------------------|
| Email    | `superadmin@myonline.com`     |
| Password | `Admin@12345`                 |
| Role     | `SUPER_ADMIN`                 |
| Portal   | `SYSADMP`                     |

> **Important:** Change this password immediately after the first login.

### Seed Roles
| Role Name    | Portal   | Description                      |
|--------------|----------|----------------------------------|
| `SUPER_ADMIN`| SYSADMP  | Full platform access             |
| `SHOP_ADMIN` | SHPADMP  | Full shop management access      |

### Seed Permissions (selected)
| Permission          | Portal   | Description                        |
|---------------------|----------|------------------------------------|
| `TENANT_CREATE`     | SYSADMP  | Create new shop/tenant             |
| `TENANT_MANAGE`     | SYSADMP  | Manage tenants                     |
| `PRODUCT_CREATE`    | SHPADMP  | Create products                    |
| `ORDER_VIEW`        | SHPADMP  | View orders                        |
| `SYS_USER_MANAGE`   | SYSADMP  | Manage system admin users          |
| _(+ 30 more)_       |          | See data.sql for full list         |

---

## 3. Postman Testing Guide

Import the following requests into Postman. Set environment variable `base_url = http://localhost:8081`.

---

### 3.1 Authentication Endpoints

#### Login
```
POST {{base_url}}/api/auth/login
Content-Type: application/json

{
  "email": "superadmin@myonline.com",
  "password": "Admin@12345"
}
```
**Expected:** `200 OK` with `accessToken` and `refreshToken`  
**Action:** Copy `accessToken` → set Postman collection variable `token = <accessToken>`

---

#### Logout
```
POST {{base_url}}/api/auth/logout
Authorization: Bearer {{token}}
```
**Expected:** `200 OK` — refresh token is revoked

---

#### Refresh Token
```
POST {{base_url}}/api/auth/refresh-token
Content-Type: application/json

{
  "refreshToken": "<refreshToken from login response>"
}
```
**Expected:** `200 OK` with a new `accessToken`

---

#### Change Password
```
PUT {{base_url}}/api/auth/change-password
Authorization: Bearer {{token}}
Content-Type: application/json

{
  "currentPassword": "Admin@12345",
  "newPassword": "NewAdmin@2024",
  "confirmPassword": "NewAdmin@2024"
}
```
**Expected:** `200 OK` — session is invalidated, login required with new password

---

### 3.2 Permission Management

#### Get All Permissions
```
GET {{base_url}}/api/permissions
Authorization: Bearer {{token}}
```
**Expected:** `200 OK` with list of all permissions

---

#### Get Permission by ID
```
GET {{base_url}}/api/permissions/1
Authorization: Bearer {{token}}
```
**Expected:** `200 OK`

---

#### Get Permissions by Portal
```
GET {{base_url}}/api/permissions/portal/SHPADMP
Authorization: Bearer {{token}}
```
**Expected:** `200 OK` with SHPADMP permissions only

---

#### Create Permission
```
POST {{base_url}}/api/permissions
Authorization: Bearer {{token}}
Content-Type: application/json

{
  "permissionTitle": "CUSTOM_REPORT",
  "permissionDescription": "Access to custom reports",
  "permissionFor": "SHPADMP"
}
```
**Expected:** `201 Created`

---

#### Update Permission
```
PUT {{base_url}}/api/permissions/1
Authorization: Bearer {{token}}
Content-Type: application/json

{
  "permissionDescription": "Updated description"
}
```
**Expected:** `200 OK`

---

#### Activate Permission
```
PUT {{base_url}}/api/permissions/1/activate
Authorization: Bearer {{token}}
```
**Expected:** `200 OK` with `permissionStatus: 1`

---

#### Deactivate Permission
```
PUT {{base_url}}/api/permissions/1/deactivate
Authorization: Bearer {{token}}
```
**Expected:** `200 OK` with `permissionStatus: 0`

---

#### Delete Permission
```
DELETE {{base_url}}/api/permissions/99
Authorization: Bearer {{token}}
```
**Expected:** `200 OK`  
**Note:** Use an ID of a non-seeded permission

---

### 3.3 Role Management

#### Get All Roles
```
GET {{base_url}}/api/roles
Authorization: Bearer {{token}}
```
**Expected:** `200 OK` with `SUPER_ADMIN` and `SHOP_ADMIN` in the list

---

#### Get Role by ID
```
GET {{base_url}}/api/roles/1
Authorization: Bearer {{token}}
```
**Expected:** `200 OK` with associated permissions

---

#### Get Roles by Shop
```
GET {{base_url}}/api/roles/shop/1
Authorization: Bearer {{token}}
```
**Expected:** `200 OK` with roles belonging to shop ID 1

---

#### Create Role
```
POST {{base_url}}/api/roles
Authorization: Bearer {{token}}
Content-Type: application/json

{
  "roleName": "INVENTORY_MANAGER",
  "roleDescription": "Manages shop inventory",
  "roleFor": "SHPADMP",
  "shopId": 1
}
```
**Expected:** `201 Created`

---

#### Update Role
```
PUT {{base_url}}/api/roles/3
Authorization: Bearer {{token}}
Content-Type: application/json

{
  "roleDescription": "Updated description for inventory manager"
}
```
**Expected:** `200 OK`

---

#### Activate / Deactivate Role
```
PUT {{base_url}}/api/roles/3/activate
PUT {{base_url}}/api/roles/3/deactivate
Authorization: Bearer {{token}}
```
**Expected:** `200 OK` with updated `roleStatus`

---

#### Assign Permissions to Role
```
POST {{base_url}}/api/roles/3/permissions
Authorization: Bearer {{token}}
Content-Type: application/json

{
  "permissionIds": [16, 17, 18]
}
```
**Expected:** `200 OK` with updated permissions list

---

#### Remove Permissions from Role
```
DELETE {{base_url}}/api/roles/3/permissions
Authorization: Bearer {{token}}
Content-Type: application/json

{
  "permissionIds": [18]
}
```
**Expected:** `200 OK` with updated permissions list

---

#### Delete Role
```
DELETE {{base_url}}/api/roles/3
Authorization: Bearer {{token}}
```
**Expected:** `200 OK`

---

### 3.4 User Management

#### Get All Users
```
GET {{base_url}}/api/users
Authorization: Bearer {{token}}
```
**Expected:** `200 OK` with the seeded Super Admin user

---

#### Get User by ID
```
GET {{base_url}}/api/users/1
Authorization: Bearer {{token}}
```
**Expected:** `200 OK` (password not included in response)

---

#### Get Users by Shop
```
GET {{base_url}}/api/users/shop/1
Authorization: Bearer {{token}}
```
**Expected:** `200 OK` with users belonging to shop ID 1

---

#### Create User (Shop Admin)
```
POST {{base_url}}/api/users
Authorization: Bearer {{token}}
Content-Type: application/json

{
  "name": "John Shop Owner",
  "mobile": "+8801811111111",
  "email": "john@shopone.com",
  "password": "Secret@123",
  "userFor": "SHPADMP",
  "shopId": 1
}
```
**Expected:** `201 Created`

---

#### Create User (System Admin)
```
POST {{base_url}}/api/users
Authorization: Bearer {{token}}
Content-Type: application/json

{
  "name": "Admin Support",
  "mobile": "+8801922222222",
  "email": "support@myonline.com",
  "password": "Support@123",
  "userFor": "SYSADMP",
  "shopId": null
}
```
**Expected:** `201 Created`

---

#### Activate / Deactivate User
```
PUT {{base_url}}/api/users/2/activate
PUT {{base_url}}/api/users/2/deactivate
Authorization: Bearer {{token}}
```
**Expected:** `200 OK` with updated `userStatus`  
**Note:** Deactivate revokes active sessions for the user

---

#### Assign Roles to User
```
POST {{base_url}}/api/users/2/roles
Authorization: Bearer {{token}}
Content-Type: application/json

{
  "roleIds": [2]
}
```
**Expected:** `200 OK` with updated roles list

---

#### Remove Roles from User
```
DELETE {{base_url}}/api/users/2/roles
Authorization: Bearer {{token}}
Content-Type: application/json

{
  "roleIds": [2]
}
```
**Expected:** `200 OK`

---

#### Delete User
```
DELETE {{base_url}}/api/users/2
Authorization: Bearer {{token}}
```
**Expected:** `200 OK`

---

## 4. Error Scenarios to Test

| Scenario                          | Expected HTTP Code |
|-----------------------------------|--------------------|
| Login with wrong password         | `401 Unauthorized` |
| Access protected endpoint without token | `403 Forbidden` |
| Access protected endpoint with expired token | `403 Forbidden` |
| Use revoked/expired refresh token | `401 Unauthorized` |
| Create user with duplicate email  | `409 Conflict`     |
| Create role with duplicate name   | `409 Conflict`     |
| Get user/role/permission with invalid ID | `404 Not Found` |
| Change password with wrong current password | `401 Unauthorized` |
| Submit request with missing required fields | `400 Bad Request` |
| New password mismatch with confirm | `400 Bad Request` |

---

## 5. Swagger UI Testing

1. Open `http://localhost:8081/swagger-ui.html`
2. Click **Authorize** (top-right)
3. Login via `POST /api/auth/login` to get access token
4. Enter: `Bearer <your_access_token>`
5. All protected endpoints are now testable directly from the browser

---

## 6. Database Verification

Connect to MySQL (`localhost:3306`, user: `root`, password: `root`, schema: `myonline`) and run:

```sql
-- Verify tables were created
SHOW TABLES;

-- Check seeded permissions
SELECT permission_title, permission_for, permission_status FROM permission;

-- Check seeded roles
SELECT role_name, role_for, role_status FROM role;

-- Check default super admin user
SELECT name, email, user_for, user_status FROM user;

-- Check role-permission assignments
SELECT r.role_name, p.permission_title
FROM role_permission rp
JOIN role r ON rp.role_id = r.role_id
JOIN permission p ON rp.permission_id = p.permission_id
ORDER BY r.role_name;

-- Check user-role assignments
SELECT u.email, r.role_name
FROM user_role ur
JOIN user u ON ur.user_id = u.user_id
JOIN role r ON ur.role_id = r.role_id;
```

---

## 7. Notes

- **JWT Access Token** expires after **15 minutes** (configurable via `app.jwt.expiration-ms`)
- **Refresh Token** expires after **7 days** (configurable via `app.jwt.refresh-expiration-ms`)
- Passwords are stored as **BCrypt hashes** (never in plain text)
- The system enforces **one active refresh token per user** — logging in again revokes the previous one
- Deactivating a user immediately **revokes their active sessions**
- All API responses follow a consistent `ApiResponse<T>` wrapper format
