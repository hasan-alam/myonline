-- =============================================================
-- Seed Data for Tenant Microservice
-- =============================================================
-- Populates the tenant_fees table with default subscription packages.
-- Uses INSERT IGNORE for idempotency (safe to run on every startup).
-- =============================================================

-- -------------------------------------------------------------
-- TENANT FEES (Subscription Packages)
-- -------------------------------------------------------------
-- Package Tiers:
--   STARTER    :   1 –   50 products   | Reg:  5,000 | Monthly:  1,000
--   BASIC      :  51 –  100 products   | Reg:  8,000 | Monthly:  1,500
--   STANDARD   : 101 –  250 products   | Reg: 12,000 | Monthly:  2,500
--   PREMIUM    : 251 –  500 products   | Reg: 18,000 | Monthly:  4,000
--   ENTERPRISE : 501 – 1000 products   | Reg: 25,000 | Monthly:  6,000
-- -------------------------------------------------------------

INSERT IGNORE INTO tenant_fees (package_code, package_name, product_count_from, product_count_to, registration_fee, monthly_fee)
VALUES
    ('STARTER',    'Starter Package',    1,   50,  5000,  1000),
    ('BASIC',      'Basic Package',     51,  100,  8000,  1500),
    ('STANDARD',   'Standard Package', 101,  250, 12000,  2500),
    ('PREMIUM',    'Premium Package',  251,  500, 18000,  4000),
    ('ENTERPRISE', 'Enterprise Package', 501, 1000, 25000, 6000);
