CREATE DATABASE IF NOT EXISTS freshbasket_db;
USE freshbasket_db;

DROP TABLE IF EXISTS order_items;
DROP TABLE IF EXISTS orders;
DROP TABLE IF EXISTS coupons;
DROP TABLE IF EXISTS products;
DROP TABLE IF EXISTS users;

CREATE TABLE users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(100) NOT NULL,
    phone VARCHAR(20),
    address TEXT,
    role ENUM('CUSTOMER', 'ADMIN') DEFAULT 'CUSTOMER',
    wallet_balance DECIMAL(10,2) DEFAULT 1500.00,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE products (
    product_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    category VARCHAR(60) NOT NULL,
    unit_weight VARCHAR(30) NOT NULL,
    mrp DECIMAL(10,2) NOT NULL,
    selling_price DECIMAL(10,2) NOT NULL,
    stock_qty INT NOT NULL DEFAULT 0,
    badge_tag VARCHAR(40) DEFAULT 'fresho!'
);

CREATE TABLE coupons (
    coupon_id INT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(30) UNIQUE NOT NULL,
    discount_type ENUM('PERCENT', 'FLAT') NOT NULL,
    discount_value DECIMAL(10,2) NOT NULL,
    min_order_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    max_discount DECIMAL(10,2) NOT NULL DEFAULT 500.00,
    is_active BOOLEAN DEFAULT TRUE
);

CREATE TABLE orders (
    order_id INT AUTO_INCREMENT PRIMARY KEY,
    order_code VARCHAR(30) UNIQUE NOT NULL,
    user_id INT NOT NULL,
    subtotal DECIMAL(10,2) NOT NULL,
    gst_amount DECIMAL(10,2) NOT NULL,
    delivery_fee DECIMAL(10,2) NOT NULL,
    discount_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    grand_total DECIMAL(10,2) NOT NULL,
    delivery_slot VARCHAR(60) NOT NULL,
    delivery_address TEXT NOT NULL,
    payment_mode VARCHAR(40) NOT NULL,
    status ENUM('Order Placed', 'Packed', 'Out for Delivery', 'Delivered') DEFAULT 'Order Placed',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

CREATE TABLE order_items (
    item_id INT AUTO_INCREMENT PRIMARY KEY,
    order_id INT NOT NULL,
    product_id INT NOT NULL,
    product_name VARCHAR(120) NOT NULL,
    unit_weight VARCHAR(30) NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(10,2) NOT NULL,
    total_price DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (order_id) REFERENCES orders(order_id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(product_id)
);

-- Seed Default Accounts
INSERT INTO users (full_name, email, password, phone, address, role, wallet_balance) VALUES
('Store Manager (Admin)', 'admin@freshbasket.com', 'admin123', '+91 98765 00001', 'FreshBasket Hub, Anna Nagar, Madurai - 625020', 'ADMIN', 0.00),
('Devaki Sivakumar', 'devaki@freshbasket.com', 'devaki123', '+91 98765 43210', '42, KK Nagar East, Madurai, Tamil Nadu - 625020', 'CUSTOMER', 2500.00);

-- Seed Coupons
INSERT INTO coupons (code, discount_type, discount_value, min_order_amount, max_discount, is_active) VALUES
('FRESH20', 'PERCENT', 20.00, 299.00, 200.00, TRUE),
('FIRST100', 'FLAT', 100.00, 399.00, 100.00, TRUE),
('BASKET50', 'FLAT', 50.00, 199.00, 50.00, TRUE);

-- Seed 24 Grocery Items across 5 Categories
INSERT INTO products (name, category, unit_weight, mrp, selling_price, stock_qty, badge_tag) VALUES
('Organic Kashmiri Apples', 'Fruits & Vegetables', '1 kg', 220.00, 176.00, 45, 'fresho!'),
('Robusta Bananas (Farm Fresh)', 'Fruits & Vegetables', '1 dozen', 65.00, 48.00, 60, 'fresho!'),
('Local Tomato - Premium', 'Fruits & Vegetables', '1 kg', 45.00, 28.00, 80, 'fresho!'),
('Carrot - Orange (Ooty)', 'Fruits & Vegetables', '500 g', 48.00, 36.00, 35, 'fresho!'),
('Coriander Leaves Without Roots', 'Fruits & Vegetables', '250 g', 35.00, 24.00, 8, 'fresho!'),
('Potato - Fresh Harvest', 'Fruits & Vegetables', '1 kg', 42.00, 29.00, 65, 'fresho!'),

('Aavin Full Cream Milk', 'Dairy & Bakery', '1 L', 68.00, 64.00, 100, 'Aavin'),
('Amul Pasteurised Butter', 'Dairy & Bakery', '500 g', 285.00, 268.00, 40, 'Amul'),
('Britannia 100% Whole Wheat Bread', 'Dairy & Bakery', '400 g', 55.00, 49.00, 30, 'Britannia'),
('Farm Fresh Brown Eggs (Pack of 6)', 'Dairy & Bakery', '6 pcs', 72.00, 58.00, 50, 'fresho!'),
('Milky Mist Fresh Paneer', 'Dairy & Bakery', '200 g', 110.00, 92.00, 6, 'Milky Mist'),

('India Gate Classic Basmati Rice', 'Staples & Grains', '5 kg', 745.00, 589.00, 28, 'India Gate'),
('Aashirvaad Shudh Chakki Atta', 'Staples & Grains', '5 kg', 295.00, 249.00, 42, 'Aashirvaad'),
('Unpolished Toor Dal (Arhar)', 'Staples & Grains', '1 kg', 185.00, 154.00, 55, 'bb Royal'),
('Fortune Sunlite Sunflower Oil', 'Staples & Grains', '1 L', 165.00, 138.00, 65, 'Fortune'),
('Tata Sampann Turmeric Powder', 'Staples & Grains', '200 g', 78.00, 64.00, 48, 'Tata Sampann'),

('Red Label Natural Care Tea', 'Snacks & Beverages', '500 g', 290.00, 245.00, 34, 'Red Label'),
('Nescafe Classic Instant Coffee', 'Snacks & Beverages', '100 g', 340.00, 295.00, 25, 'Nescafe'),
('Paper Boat Alphonso Aamras', 'Snacks & Beverages', '1 L', 120.00, 89.00, 40, 'Paper Boat'),
('Lay''s Magic Masala Chips', 'Snacks & Beverages', '90 g', 50.00, 30.00, 75, 'Lay''s'),

('Surf Excel Matic Liquid Detergent', 'Household', '2 L', 460.00, 379.00, 30, 'Surf Excel'),
('Vim Dishwash Gel Lemon Refill', 'Household', '750 ml', 175.00, 142.00, 52, 'Vim'),
('Lizol Disinfectant Floor Cleaner', 'Household', '2 L', 399.00, 319.00, 7, 'Lizol'),
('Origami Soft Facial Tissues Box', 'Household', '200 pulls', 140.00, 99.00, 45, 'Origami');
