-- =============================================
-- Seed Data for Authorization Microservice
-- myonline Multi-Tenant E-Commerce Platform
-- =============================================
-- Uses INSERT IGNORE to ensure idempotency on every startup.
-- Safe to run multiple times without duplication.

-- =============================================
-- 0. CLEANUP: Remove obsolete coarse-grained permissions
--    (replaced by granular permissions below)
-- =============================================

DELETE FROM role_permission
WHERE permission_id IN (
    SELECT permission_id FROM permission
    WHERE permission_title IN (
        -- System Admin: replaced by granular SYS_CATEGORY_* permissions
        'SYS_CATEGORY_MANAGE',
        -- System Admin: replaced by granular SYS_PRODUCT_* permissions (renamed from ITEM to PRODUCT)
        'SYS_ITEM_MANAGE',
        -- System Admin: replaced by TENANT_PAYMENT_VIEW + TENANT_PAYMENT_APPROVAL
        'TENANT_PAYMENT_REPORT',
        -- System Admin: replaced by granular SYS_ROLE_* permissions
        'SYS_ROLE_MANAGE',
        -- Shop Admin: replaced by granular INVENTORY_* permissions
        'INVENTORY_MANAGE',
        -- Shop Admin: replaced by granular CATEGORY_* permissions
        'CATEGORY_MANAGE',
        -- Shop Admin: replaced by granular SHIPPER_* permissions
        'SHIPPER_MANAGE',
        -- Shop Admin: replaced by granular SHOP_ROLE_* permissions
        'SHOP_ROLE_MANAGE'
    )
);

DELETE FROM permission
WHERE permission_title IN (
    'SYS_CATEGORY_MANAGE',
    'SYS_ITEM_MANAGE',
    'TENANT_PAYMENT_REPORT',
    'SYS_ROLE_MANAGE',
    'INVENTORY_MANAGE',
    'CATEGORY_MANAGE',
    'SHIPPER_MANAGE',
    'SHOP_ROLE_MANAGE'
);

-- =============================================
-- 1. PERMISSIONS (System Admin Portal)
-- =============================================

INSERT IGNORE INTO permission (permission_title, permission_description, permission_status, permission_for, created_at, updated_at)
VALUES
    -- Tenant Management
    ('TENANT_CREATE',           'Create new shop/tenant',                           1, 'SYSADMP', NOW(), NOW()),
    ('TENANT_VIEW',             'View shop/tenant details',                         1, 'SYSADMP', NOW(), NOW()),
    ('TENANT_EDIT',             'Edit shop/tenant information',                     1, 'SYSADMP', NOW(), NOW()),
    ('TENANT_ACTIVATE',         'Activate a shop/tenant',                           1, 'SYSADMP', NOW(), NOW()),
    ('TENANT_DEACTIVATE',       'Deactivate a shop/tenant',                         1, 'SYSADMP', NOW(), NOW()),
    ('TENANT_DELETE',           'Delete a shop/tenant',                             1, 'SYSADMP', NOW(), NOW()),

    -- System Product Management (category)
    ('SYS_CATEGORY_VIEW',       'View product categories (system-level)',            1, 'SYSADMP', NOW(), NOW()),
    ('SYS_CATEGORY_CREATE',     'Create product categories (system-level)',          1, 'SYSADMP', NOW(), NOW()),
    ('SYS_CATEGORY_EDIT',       'Edit product categories (system-level)',            1, 'SYSADMP', NOW(), NOW()),
    ('SYS_CATEGORY_DELETE',     'Delete product categories (system-level)',          1, 'SYSADMP', NOW(), NOW()),
    ('SYS_CATEGORY_ACTIVATE',   'Activate/deactivate product categories (system)',   1, 'SYSADMP', NOW(), NOW()),

    -- System Product Management (product — renamed from item)
    ('SYS_PRODUCT_VIEW',        'View products (system-level)',                      1, 'SYSADMP', NOW(), NOW()),
    ('SYS_PRODUCT_CREATE',      'Create products (system-level)',                    1, 'SYSADMP', NOW(), NOW()),
    ('SYS_PRODUCT_EDIT',        'Edit products (system-level)',                      1, 'SYSADMP', NOW(), NOW()),
    ('SYS_PRODUCT_DELETE',      'Delete products (system-level)',                    1, 'SYSADMP', NOW(), NOW()),
    ('SYS_PRODUCT_ACTIVATE',    'Activate/deactivate products (system-level)',       1, 'SYSADMP', NOW(), NOW()),

    -- Payment Reports
    ('TENANT_PAYMENT_VIEW',     'View tenant payment and subscription reports',      1, 'SYSADMP', NOW(), NOW()),
    ('TENANT_PAYMENT_APPROVAL', 'Approve tenant payment transactions',               1, 'SYSADMP', NOW(), NOW()),

    -- User Management (System Admin)
    ('SYS_USER_CREATE',         'Create system admin users',                        1, 'SYSADMP', NOW(), NOW()),
    ('SYS_USER_VIEW',           'View system admin users',                          1, 'SYSADMP', NOW(), NOW()),
    ('SYS_USER_MANAGE',         'Activate/deactivate system admin users',           1, 'SYSADMP', NOW(), NOW()),
    ('SYS_USER_DELETE',         'Delete system admin users',                        1, 'SYSADMP', NOW(), NOW()),

    -- Role Management (System Admin) — granular
    ('SYS_ROLE_VIEW',           'View roles (system-level)',                        1, 'SYSADMP', NOW(), NOW()),
    ('SYS_ROLE_CREATE',         'Create roles (system-level)',                      1, 'SYSADMP', NOW(), NOW()),
    ('SYS_ROLE_EDIT',           'Edit roles and assign/remove permissions (system)',1, 'SYSADMP', NOW(), NOW()),
    ('SYS_ROLE_DELETE',         'Delete roles (system-level)',                      1, 'SYSADMP', NOW(), NOW()),
    ('SYS_ROLE_ACTIVATE',       'Activate/deactivate roles (system-level)',         1, 'SYSADMP', NOW(), NOW()),

    -- Permission Management (System Admin)
    ('SYS_PERMISSION_MANAGE',   'Create, edit, delete and manage permissions',      1, 'SYSADMP', NOW(), NOW());

-- PERMISSION_VIEW is for BOTH portals (Shop Admin Portal users also need to view permissions
-- when configuring role assignments)
INSERT IGNORE INTO permission (permission_title, permission_description, permission_status, permission_for, created_at, updated_at)
VALUES
    ('PERMISSION_VIEW',         'View permissions list (all portals)',               1, 'BOTH',    NOW(), NOW());

-- =============================================
-- 2. PERMISSIONS (Shop Admin Portal)
-- =============================================

INSERT IGNORE INTO permission (permission_title, permission_description, permission_status, permission_for, created_at, updated_at)
VALUES
    -- Product & Inventory — granular inventory permissions
    ('INVENTORY_VIEW',          'View inventory levels',                            1, 'SHPADMP', NOW(), NOW()),
    ('INVENTORY_ADD',           'Add inventory stock',                              1, 'SHPADMP', NOW(), NOW()),
    ('INVENTORY_EDIT',          'Edit inventory records',                           1, 'SHPADMP', NOW(), NOW()),
    ('INVENTORY_DELETE',        'Delete inventory records',                         1, 'SHPADMP', NOW(), NOW()),

    -- Product & Inventory — granular category permissions (shop-level)
    ('CATEGORY_VIEW',           'View product categories (shop-level)',             1, 'SHPADMP', NOW(), NOW()),
    ('CATEGORY_ADD',            'Add product categories (shop-level)',              1, 'SHPADMP', NOW(), NOW()),
    ('CATEGORY_EDIT',           'Edit product categories (shop-level)',             1, 'SHPADMP', NOW(), NOW()),
    ('CATEGORY_DELETE',         'Delete product categories (shop-level)',           1, 'SHPADMP', NOW(), NOW()),

    -- Product Management (existing granular product permissions)
    ('PRODUCT_VIEW',            'View products and inventory',                      1, 'SHPADMP', NOW(), NOW()),
    ('PRODUCT_CREATE',          'Create new products',                              1, 'SHPADMP', NOW(), NOW()),
    ('PRODUCT_EDIT',            'Edit existing products',                           1, 'SHPADMP', NOW(), NOW()),
    ('PRODUCT_DELETE',          'Delete products',                                  1, 'SHPADMP', NOW(), NOW()),
    ('PRODUCT_ACTIVATE',        'Activate/deactivate products',                     1, 'SHPADMP', NOW(), NOW()),

    -- Order Management
    ('ORDER_VIEW',              'View orders',                                      1, 'SHPADMP', NOW(), NOW()),
    ('ORDER_CONFIRM',           'Confirm orders',                                   1, 'SHPADMP', NOW(), NOW()),
    ('ORDER_SHIP',              'Mark orders as shipped',                           1, 'SHPADMP', NOW(), NOW()),
    ('ORDER_CANCEL',            'Cancel orders',                                    1, 'SHPADMP', NOW(), NOW()),
    ('ORDER_RETURN',            'Process order returns',                            1, 'SHPADMP', NOW(), NOW()),

    -- Shipper Management — granular
    ('SHIPPER_VIEW',            'View shippers and delivery agents',                1, 'SHPADMP', NOW(), NOW()),
    ('SHIPPER_ADD',             'Add new shippers',                                 1, 'SHPADMP', NOW(), NOW()),
    ('SHIPPER_EDIT',            'Edit shipper information',                         1, 'SHPADMP', NOW(), NOW()),
    ('SHIPPER_DELETE',          'Delete shippers',                                  1, 'SHPADMP', NOW(), NOW()),

    ('PAYMENT_RECEIVABLE',      'Manage payment collection from shippers',          1, 'SHPADMP', NOW(), NOW()),

    -- Reports
    ('REPORT_VIEW',             'View shop reports',                                1, 'SHPADMP', NOW(), NOW()),

    -- User Management (Shop Admin)
    ('SHOP_USER_CREATE',        'Create shop admin users',                          1, 'SHPADMP', NOW(), NOW()),
    ('SHOP_USER_VIEW',          'View shop users',                                  1, 'SHPADMP', NOW(), NOW()),
    ('SHOP_USER_MANAGE',        'Activate/deactivate shop users',                   1, 'SHPADMP', NOW(), NOW()),
    ('SHOP_USER_DELETE',        'Delete shop users',                                1, 'SHPADMP', NOW(), NOW()),

    -- Role Management (Shop Admin) — granular
    ('SHOP_ROLE_VIEW',          'View roles (shop-level)',                          1, 'SHPADMP', NOW(), NOW()),
    ('SHOP_ROLE_ADD',           'Create roles (shop-level)',                        1, 'SHPADMP', NOW(), NOW()),
    ('SHOP_ROLE_EDIT',          'Edit roles and assign/remove permissions (shop)',  1, 'SHPADMP', NOW(), NOW()),
    ('SHOP_ROLE_DELETE',        'Delete roles (shop-level)',                        1, 'SHPADMP', NOW(), NOW()),

    -- Subscription
    ('SUBSCRIPTION_MANAGE',     'Manage shop subscription payments',                1, 'SHPADMP', NOW(), NOW());

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

-- Assign all SYSADMP and BOTH permissions to SUPER_ADMIN role (skip if already assigned)
INSERT INTO role_permission (role_id, permission_id)
SELECT r.role_id, p.permission_id
FROM role r, permission p
WHERE r.role_name = 'SUPER_ADMIN'
  AND p.permission_for IN ('SYSADMP', 'BOTH')
  AND NOT EXISTS (
      SELECT 1 FROM role_permission rp
      WHERE rp.role_id = r.role_id AND rp.permission_id = p.permission_id
  );

-- =============================================
-- 5. ASSIGN SHOP PERMISSIONS TO SHOP_ADMIN
-- =============================================

-- Assign all SHPADMP and BOTH permissions to SHOP_ADMIN role (skip if already assigned)
INSERT INTO role_permission (role_id, permission_id)
SELECT r.role_id, p.permission_id
FROM role r, permission p
WHERE r.role_name = 'SHOP_ADMIN'
  AND p.permission_for IN ('SHPADMP', 'BOTH')
  AND NOT EXISTS (
      SELECT 1 FROM role_permission rp
      WHERE rp.role_id = r.role_id AND rp.permission_id = p.permission_id
  );

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
    '$2a$12$Z7L5iuo/V5tuDC3Lb0iLzu7lcYzv72vL6gyF9lFrwbOTHgRou2A72',
    'SYSADMP',
    1,
    NULL,
    NOW(),
    NOW()
);

-- =============================================
-- 7. ASSIGN SUPER_ADMIN ROLE TO DEFAULT USER
-- =============================================

INSERT INTO user_role (user_id, role_id)
SELECT u.user_id, r.role_id
FROM user u, role r
WHERE u.email = 'superadmin@myonline.com'
  AND r.role_name = 'SUPER_ADMIN'
  AND NOT EXISTS (
      SELECT 1 FROM user_role ur
      WHERE ur.user_id = u.user_id AND ur.role_id = r.role_id
  );
