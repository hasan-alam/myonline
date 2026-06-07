-- =============================================
-- Seed Data for Authorization Microservice
-- myonline Multi-Tenant E-Commerce Platform
-- =============================================
-- Uses INSERT IGNORE to ensure idempotency on every startup.
-- Safe to run multiple times without duplication.

-- =============================================
-- 1. PERMISSIONS (System Admin Portal)
-- =============================================

INSERT IGNORE INTO permission (permission_title, permission_description, permission_status, permission_for, created_at, updated_at)
VALUES
    -- Tenant Management
    ('TENANT_CREATE',      'Create new shop/tenant',               1, 'SYSADMP', NOW(), NOW()),
    ('TENANT_VIEW',        'View shop/tenant details',             1, 'SYSADMP', NOW(), NOW()),
    ('TENANT_EDIT',        'Edit shop/tenant information',         1, 'SYSADMP', NOW(), NOW()),
    ('TENANT_ACTIVATE',    'Activate a shop/tenant',               1, 'SYSADMP', NOW(), NOW()),
    ('TENANT_DEACTIVATE',  'Deactivate a shop/tenant',             1, 'SYSADMP', NOW(), NOW()),
    ('TENANT_DELETE',      'Delete a shop/tenant',                 1, 'SYSADMP', NOW(), NOW()),

    -- System Product Management
    ('SYS_CATEGORY_MANAGE',    'Manage product categories (system-level)',   1, 'SYSADMP', NOW(), NOW()),
    ('SYS_ITEM_MANAGE',        'Manage items/products (system-level)',        1, 'SYSADMP', NOW(), NOW()),

    -- Payment Reports
    ('TENANT_PAYMENT_REPORT',  'View tenant payment and subscription reports', 1, 'SYSADMP', NOW(), NOW()),

    -- User Management (System Admin)
    ('SYS_USER_CREATE',    'Create system admin users',            1, 'SYSADMP', NOW(), NOW()),
    ('SYS_USER_VIEW',      'View system admin users',              1, 'SYSADMP', NOW(), NOW()),
    ('SYS_USER_MANAGE',    'Activate/deactivate system admin users', 1, 'SYSADMP', NOW(), NOW()),
    ('SYS_USER_DELETE',    'Delete system admin users',            1, 'SYSADMP', NOW(), NOW()),

    -- Role & Permission Management (System Admin)
    ('SYS_ROLE_MANAGE',       'Manage roles (system-level)',          1, 'SYSADMP', NOW(), NOW()),
    ('SYS_PERMISSION_MANAGE', 'Manage permissions (system-level)',    1, 'SYSADMP', NOW(), NOW());

-- =============================================
-- 2. PERMISSIONS (Shop Admin Portal)
-- =============================================

INSERT IGNORE INTO permission (permission_title, permission_description, permission_status, permission_for, created_at, updated_at)
VALUES
    -- Product & Inventory
    ('PRODUCT_VIEW',          'View products and inventory',         1, 'SHPADMP', NOW(), NOW()),
    ('PRODUCT_CREATE',        'Create new products',                 1, 'SHPADMP', NOW(), NOW()),
    ('PRODUCT_EDIT',          'Edit existing products',              1, 'SHPADMP', NOW(), NOW()),
    ('PRODUCT_DELETE',        'Delete products',                     1, 'SHPADMP', NOW(), NOW()),
    ('PRODUCT_ACTIVATE',      'Activate/deactivate products',        1, 'SHPADMP', NOW(), NOW()),
    ('INVENTORY_MANAGE',      'Manage inventory levels',             1, 'SHPADMP', NOW(), NOW()),
    ('CATEGORY_MANAGE',       'Manage product categories',           1, 'SHPADMP', NOW(), NOW()),

    -- Order Management
    ('ORDER_VIEW',            'View orders',                         1, 'SHPADMP', NOW(), NOW()),
    ('ORDER_CONFIRM',         'Confirm orders',                      1, 'SHPADMP', NOW(), NOW()),
    ('ORDER_SHIP',            'Mark orders as shipped',              1, 'SHPADMP', NOW(), NOW()),
    ('ORDER_CANCEL',          'Cancel orders',                       1, 'SHPADMP', NOW(), NOW()),
    ('ORDER_RETURN',          'Process order returns',               1, 'SHPADMP', NOW(), NOW()),

    -- Shipper Management
    ('SHIPPER_MANAGE',        'Manage shippers and delivery agents', 1, 'SHPADMP', NOW(), NOW()),
    ('PAYMENT_RECEIVABLE',    'Manage payment collection from shippers', 1, 'SHPADMP', NOW(), NOW()),

    -- Reports
    ('REPORT_VIEW',           'View shop reports',                   1, 'SHPADMP', NOW(), NOW()),

    -- User Management (Shop Admin)
    ('SHOP_USER_CREATE',      'Create shop admin users',             1, 'SHPADMP', NOW(), NOW()),
    ('SHOP_USER_VIEW',        'View shop users',                     1, 'SHPADMP', NOW(), NOW()),
    ('SHOP_USER_MANAGE',      'Activate/deactivate shop users',      1, 'SHPADMP', NOW(), NOW()),
    ('SHOP_USER_DELETE',      'Delete shop users',                   1, 'SHPADMP', NOW(), NOW()),

    -- Role & Permission Management (Shop Admin)
    ('SHOP_ROLE_MANAGE',      'Manage roles (shop-level)',           1, 'SHPADMP', NOW(), NOW()),

    -- Subscription
    ('SUBSCRIPTION_MANAGE',   'Manage shop subscription payments',   1, 'SHPADMP', NOW(), NOW());

-- =============================================
-- 3. ROLES (Seed Roles)
-- =============================================

-- Super Admin: Full access to System Admin Portal (no shop_id — system level)
INSERT IGNORE INTO role (role_name, role_description, role_status, role_for, shop_id, created_at, updated_at)
VALUES ('SUPER_ADMIN', 'Platform super administrator with full system access', 1, 'SYSADMP', NULL, NOW(), NOW());

-- Shop Admin: Full access to Shop Admin Portal (no shop_id here — it will be set per tenant)
INSERT IGNORE INTO role (role_name, role_description, role_status, role_for, shop_id, created_at, updated_at)
VALUES ('SHOP_ADMIN', 'Shop administrator with full shop management access', 1, 'SHPADMP', NULL, NOW(), NOW());

-- =============================================
-- 4. ASSIGN ALL PERMISSIONS TO SUPER_ADMIN
-- =============================================

-- Assign all SYSADMP permissions to SUPER_ADMIN role
INSERT IGNORE INTO role_permission (role_id, permission_id)
SELECT r.role_id, p.permission_id
FROM role r, permission p
WHERE r.role_name = 'SUPER_ADMIN'
  AND p.permission_for IN ('SYSADMP', 'BOTH');

-- =============================================
-- 5. ASSIGN SHOP PERMISSIONS TO SHOP_ADMIN
-- =============================================

-- Assign all SHPADMP permissions to SHOP_ADMIN role
INSERT IGNORE INTO role_permission (role_id, permission_id)
SELECT r.role_id, p.permission_id
FROM role r, permission p
WHERE r.role_name = 'SHOP_ADMIN'
  AND p.permission_for IN ('SHPADMP', 'BOTH');

-- =============================================
-- 6. DEFAULT SUPER ADMIN USER
-- =============================================
-- Default password: Admin@12345
-- BCrypt hash of "Admin@12345" (strength 12)
-- IMPORTANT: Change this password immediately after first login!

INSERT IGNORE INTO user (name, mobile, email, password, user_for, user_status, shop_id, created_at, updated_at)
VALUES (
    'System Super Admin',
    '+8801700000000',
    'superadmin@myonline.com',
    '$2a$12$ov/GITPaORQYluH4MtiGqeILlnVoA3hFgBtjkJekm2OWkEYr2.7eK',
    'SYSADMP',
    1,
    NULL,
    NOW(),
    NOW()
);

-- =============================================
-- 7. ASSIGN SUPER_ADMIN ROLE TO DEFAULT USER
-- =============================================

INSERT IGNORE INTO user_role (user_id, role_id)
SELECT u.user_id, r.role_id
FROM user u, role r
WHERE u.email = 'superadmin@myonline.com'
  AND r.role_name = 'SUPER_ADMIN';
