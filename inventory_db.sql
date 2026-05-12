-- ============================================================
--  Smart Inventory Management System
--  MySQL Database Schema  |  XAMPP  127.0.0.1:3306
--
--  HOW TO IMPORT:
--    phpMyAdmin → Import tab → choose this file → Go
--    OR: mysql -u root -p < inventory_db.sql
--
--  No seed data — all data is entered through the application.
-- ============================================================

CREATE DATABASE IF NOT EXISTS inventory_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE inventory_db;

-- ── 1. users ─────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS users (
    user_id    INT            NOT NULL AUTO_INCREMENT,
    username   VARCHAR(50)    NOT NULL UNIQUE,
    password   VARCHAR(255)   NOT NULL,
    full_name  VARCHAR(100)   NOT NULL,
    role       ENUM('admin','staff') NOT NULL DEFAULT 'staff',
    is_active  TINYINT(1)     NOT NULL DEFAULT 1,
    created_at DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id)
);

-- ── 2. categories ────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS categories (
    category_id   INT          NOT NULL AUTO_INCREMENT,
    name          VARCHAR(100) NOT NULL UNIQUE,
    description   TEXT,
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (category_id)
);

-- ── 3. suppliers ─────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS suppliers (
    supplier_id   INT          NOT NULL AUTO_INCREMENT,
    name          VARCHAR(150) NOT NULL,
    contact_name  VARCHAR(100),
    phone         VARCHAR(30),
    email         VARCHAR(150),
    address       TEXT,
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (supplier_id)
);

-- ── 4. products ──────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS products (
    product_id          INT             NOT NULL AUTO_INCREMENT,
    category_id         INT,
    supplier_id         INT,
    name                VARCHAR(150)    NOT NULL,
    description         TEXT,
    quantity            INT             NOT NULL DEFAULT 0,
    price               DECIMAL(10, 2)  NOT NULL DEFAULT 0.00,
    low_stock_threshold INT             NOT NULL DEFAULT 10,
    created_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP
                                                 ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (product_id),
    CONSTRAINT fk_product_category FOREIGN KEY (category_id)
        REFERENCES categories (category_id) ON DELETE SET NULL,
    CONSTRAINT fk_product_supplier FOREIGN KEY (supplier_id)
        REFERENCES suppliers (supplier_id) ON DELETE SET NULL
);

-- ── 5. sales ─────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS sales (
    sale_id       INT             NOT NULL AUTO_INCREMENT,
    user_id       INT,
    sale_date     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    total_amount  DECIMAL(12, 2)  NOT NULL DEFAULT 0.00,
    notes         TEXT,
    PRIMARY KEY (sale_id),
    CONSTRAINT fk_sale_user FOREIGN KEY (user_id)
        REFERENCES users (user_id) ON DELETE SET NULL
);

-- ── 6. sale_items ────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS sale_items (
    sale_item_id  INT             NOT NULL AUTO_INCREMENT,
    sale_id       INT             NOT NULL,
    product_id    INT             NOT NULL,
    quantity_sold INT             NOT NULL,
    unit_price    DECIMAL(10, 2)  NOT NULL,
    subtotal      DECIMAL(12, 2)  NOT NULL,
    PRIMARY KEY (sale_item_id),
    CONSTRAINT fk_saleitem_sale    FOREIGN KEY (sale_id)
        REFERENCES sales (sale_id) ON DELETE CASCADE,
    CONSTRAINT fk_saleitem_product FOREIGN KEY (product_id)
        REFERENCES products (product_id) ON DELETE RESTRICT
);

-- ── 7. inventory_logs ────────────────────────────────────────
CREATE TABLE IF NOT EXISTS inventory_logs (
    log_id          INT          NOT NULL AUTO_INCREMENT,
    product_id      INT          NOT NULL,
    user_id         INT,
    change_type     ENUM('add','remove','adjust','sale') NOT NULL,
    quantity_before INT          NOT NULL,
    quantity_change INT          NOT NULL,
    quantity_after  INT          NOT NULL,
    note            VARCHAR(255),
    logged_at       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (log_id),
    CONSTRAINT fk_log_product FOREIGN KEY (product_id)
        REFERENCES products (product_id) ON DELETE CASCADE,
    CONSTRAINT fk_log_user    FOREIGN KEY (user_id)
        REFERENCES users (user_id) ON DELETE SET NULL
);

-- ============================================================
--  TRIGGER: auto-deduct stock on sale_item insert
-- ============================================================
DELIMITER $$

CREATE TRIGGER trg_deduct_stock
AFTER INSERT ON sale_items
FOR EACH ROW
BEGIN
    DECLARE v_before INT;
    SELECT quantity INTO v_before FROM products WHERE product_id = NEW.product_id;
    UPDATE products SET quantity = quantity - NEW.quantity_sold
    WHERE product_id = NEW.product_id;
    INSERT INTO inventory_logs (product_id, change_type, quantity_before, quantity_change, quantity_after, note)
    VALUES (NEW.product_id, 'sale', v_before, -NEW.quantity_sold,
            v_before - NEW.quantity_sold, CONCAT('Sale ID: ', NEW.sale_id));
END$$

DELIMITER ;

-- ============================================================
--  VIEWS
-- ============================================================
CREATE OR REPLACE VIEW v_low_stock AS
SELECT p.product_id, p.name, p.quantity, p.low_stock_threshold, p.price,
       c.name AS category
FROM products p
LEFT JOIN categories c ON p.category_id = c.category_id
WHERE p.quantity < p.low_stock_threshold;

CREATE OR REPLACE VIEW v_inventory_summary AS
SELECT COUNT(*)                            AS total_skus,
       COALESCE(SUM(quantity * price), 0)  AS total_value,
       SUM(quantity < low_stock_threshold) AS low_stock_count
FROM products;

CREATE OR REPLACE VIEW v_sales_report AS
SELECT s.sale_id, s.sale_date, u.full_name AS recorded_by,
       p.name AS product_name, si.quantity_sold, si.unit_price,
       si.subtotal, s.total_amount
FROM sales s
LEFT JOIN users     u  ON s.user_id     = u.user_id
JOIN  sale_items    si ON s.sale_id     = si.sale_id
JOIN  products      p  ON si.product_id = p.product_id
ORDER BY s.sale_date DESC;

-- ── Default admin account (change password after first login) ─
INSERT INTO users (username, password, full_name, role)
VALUES ('admin', 'admin123', 'Administrator', 'admin');
