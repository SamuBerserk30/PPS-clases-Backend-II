-- ============================================
-- Product Purchasing System - Initial Data
-- ============================================
--
-- Este script inserta datos iniciales para testing y desarrollo.
--
-- Para ejecutar:
-- mysql -u root -p pps_db < data.sql
-- ============================================

USE pps_db;

-- ============================================
-- Datos: roles
-- ============================================
INSERT INTO roles (role_id, name, description) VALUES
(1, 'ADMIN', 'Administrator with full access'),
(2, 'CUSTOMER', 'Regular customer user'),
(3, 'MANAGER', 'Store manager with elevated privileges')
ON DUPLICATE KEY UPDATE description=VALUES(description);

-- ============================================
-- Datos: order_statuses
-- ============================================
INSERT INTO order_statuses (order_status_id, name, description) VALUES
(1, 'PENDING', 'Order created, awaiting payment'),
(2, 'CONFIRMED', 'Payment confirmed, processing order'),
(3, 'PROCESSING', 'Order is being prepared'),
(4, 'SHIPPED', 'Order has been shipped'),
(5, 'DELIVERED', 'Order delivered successfully'),
(6, 'CANCELLED', 'Order was cancelled'),
(7, 'REFUNDED', 'Order was refunded')
ON DUPLICATE KEY UPDATE description=VALUES(description);

-- ============================================
-- Datos: payment_methods
-- ============================================
INSERT INTO payment_methods (payment_method_id, name, description) VALUES
(1, 'CREDIT_CARD', 'Credit Card Payment'),
(2, 'DEBIT_CARD', 'Debit Card Payment'),
(3, 'PAYPAL', 'PayPal Payment'),
(4, 'BANK_TRANSFER', 'Bank Transfer'),
(5, 'CASH_ON_DELIVERY', 'Cash on Delivery')
ON DUPLICATE KEY UPDATE description=VALUES(description);

-- ============================================
-- Datos: payment_statuses
-- ============================================
INSERT INTO payment_statuses (payment_status_id, name, description) VALUES
(1, 'PENDING', 'Payment pending'),
(2, 'PROCESSING', 'Payment being processed'),
(3, 'COMPLETED', 'Payment completed successfully'),
(4, 'FAILED', 'Payment failed'),
(5, 'REFUNDED', 'Payment refunded'),
(6, 'CANCELLED', 'Payment cancelled')
ON DUPLICATE KEY UPDATE description=VALUES(description);

-- ============================================
-- Datos: categories (Jerarquía de ejemplo)
-- ============================================
-- Categorías raíz
INSERT INTO categories (category_id, parent_id, name, slug) VALUES
(1, NULL, 'Electronics', 'electronics'),
(2, NULL, 'Clothing', 'clothing'),
(3, NULL, 'Books', 'books'),
(4, NULL, 'Home & Garden', 'home-garden')
ON DUPLICATE KEY UPDATE name=VALUES(name);

-- Subcategorías de Electronics
INSERT INTO categories (category_id, parent_id, name, slug) VALUES
(11, 1, 'Computers', 'computers'),
(12, 1, 'Smartphones', 'smartphones'),
(13, 1, 'Audio', 'audio'),
(14, 1, 'Cameras', 'cameras')
ON DUPLICATE KEY UPDATE name=VALUES(name);

-- Subcategorías de Clothing
INSERT INTO categories (category_id, parent_id, name, slug) VALUES
(21, 2, 'Men', 'men-clothing'),
(22, 2, 'Women', 'women-clothing'),
(23, 2, 'Kids', 'kids-clothing')
ON DUPLICATE KEY UPDATE name=VALUES(name);

-- Subcategorías de Books
INSERT INTO categories (category_id, parent_id, name, slug) VALUES
(31, 3, 'Fiction', 'fiction'),
(32, 3, 'Non-Fiction', 'non-fiction'),
(33, 3, 'Technical', 'technical-books')
ON DUPLICATE KEY UPDATE name=VALUES(name);

-- ============================================
-- Datos: products (Ejemplos)
-- ============================================
-- Productos de Electronics > Computers
INSERT INTO products (product_id, category_id, sku, name, description, price, stock_qty, is_active) VALUES
(1, 11, 'COMP-LAP-001', 'Dell XPS 13 Laptop', 'High-performance ultrabook with Intel i7', 1299.99, 15, TRUE),
(2, 11, 'COMP-LAP-002', 'MacBook Air M2', 'Apple MacBook Air with M2 chip', 1199.99, 10, TRUE),
(3, 11, 'COMP-DES-001', 'Gaming Desktop PC', 'High-end gaming desktop with RTX 4080', 2499.99, 5, TRUE)
ON DUPLICATE KEY UPDATE price=VALUES(price), stock_qty=VALUES(stock_qty);

-- Productos de Electronics > Smartphones
INSERT INTO products (product_id, category_id, sku, name, description, price, stock_qty, is_active) VALUES
(4, 2, 'PHONE-IP-001', 'iPhone 15 Pro', 'Latest Apple iPhone with A17 chip', 999.99, 20, TRUE),
(5, 2, 'PHONE-SAM-001', 'Samsung Galaxy S24', 'Flagship Samsung smartphone', 899.99, 25, TRUE),
(6, 2, 'PHONE-PIX-001', 'Google Pixel 8', 'Google Pixel with advanced AI', 699.99, 15, TRUE)
ON DUPLICATE KEY UPDATE price=VALUES(price), stock_qty=VALUES(stock_qty);

-- Productos de Electronics > Audio
INSERT INTO products (product_id, category_id, sku, name, description, price, stock_qty, is_active) VALUES
(7, 13, 'AUD-HEAD-001', 'Sony WH-1000XM5', 'Noise-canceling wireless headphones', 399.99, 30, TRUE),
(8, 13, 'AUD-SPEAK-001', 'JBL Flip 6', 'Portable Bluetooth speaker', 129.99, 50, TRUE)
ON DUPLICATE KEY UPDATE price=VALUES(price), stock_qty=VALUES(stock_qty);

-- Productos de Clothing > Men
INSERT INTO products (product_id, category_id, sku, name, description, price, stock_qty, is_active) VALUES
(9, 21, 'CLOTH-MEN-001', 'Classic Denim Jeans', 'Comfortable fit denim jeans', 59.99, 100, TRUE),
(10, 21, 'CLOTH-MEN-002', 'Cotton T-Shirt', 'Basic cotton t-shirt', 19.99, 200, TRUE)
ON DUPLICATE KEY UPDATE price=VALUES(price), stock_qty=VALUES(stock_qty);

-- Productos de Books > Technical
INSERT INTO products (product_id, category_id, sku, name, description, price, stock_qty, is_active) VALUES
(11, 33, 'BOOK-TECH-001', 'Clean Code', 'Robert C. Martin - Programming best practices', 44.99, 50, TRUE),
(12, 33, 'BOOK-TECH-002', 'Design Patterns', 'Gang of Four - Essential design patterns', 54.99, 40, TRUE),
(13, 33, 'BOOK-TECH-003', 'Java Persistence with Hibernate', 'JPA and Hibernate guide', 49.99, 30, TRUE)
ON DUPLICATE KEY UPDATE price=VALUES(price), stock_qty=VALUES(stock_qty);

-- ============================================
-- Datos: users (Ejemplos para testing)
-- ============================================
-- NOTA: Los passwords aquí son hashes de ejemplo
-- En producción usar bcrypt u otro algoritmo seguro
INSERT INTO users (user_id, role_id, email, password_hash, first_name, last_name, phone, status) VALUES
(1, 1, 'admin@pps.com', '$2a$10$example.hash.admin', 'Admin', 'User', '555-0001', 'ACTIVE'),
(2, 2, 'john.doe@email.com', '$2a$10$example.hash.john', 'John', 'Doe', '555-0100', 'ACTIVE'),
(3, 2, 'jane.smith@email.com', '$2a$10$example.hash.jane', 'Jane', 'Smith', '555-0101', 'ACTIVE'),
(4, 3, 'manager@pps.com', '$2a$10$example.hash.manager', 'Store', 'Manager', '555-0002', 'ACTIVE')
ON DUPLICATE KEY UPDATE email=VALUES(email);

-- ============================================
-- Datos: addresses (Ejemplos)
-- ============================================
INSERT INTO addresses (user_id, type, line1, line2, city, state, country, postal_code, is_default) VALUES
(2, 'SHIPPING', '123 Main Street', 'Apt 4B', 'New York', 'NY', 'USA', '10001', TRUE),
(2, 'BILLING', '123 Main Street', 'Apt 4B', 'New York', 'NY', 'USA', '10001', TRUE),
(3, 'SHIPPING', '456 Oak Avenue', NULL, 'Los Angeles', 'CA', 'USA', '90001', TRUE),
(3, 'BILLING', '456 Oak Avenue', NULL, 'Los Angeles', 'CA', 'USA', '90001', TRUE)
ON DUPLICATE KEY UPDATE line1=VALUES(line1);

-- ============================================
-- Verificación de datos insertados
-- ============================================
-- SELECT * FROM roles;
-- SELECT * FROM categories WHERE parent_id IS NULL;
-- SELECT * FROM products LIMIT 5;
-- SELECT * FROM users;
