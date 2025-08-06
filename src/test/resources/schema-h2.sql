-- 既存のテーブルがあれば削除
DROP TABLE IF EXISTS user_roles;
DROP TABLE IF EXISTS roles;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS inventories;
DROP TABLE IF EXISTS skus;
DROP TABLE IF EXISTS products;
DROP TABLE IF EXISTS categories;


-- roles テーブル
CREATE TABLE roles (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

-- users テーブル
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT(1000) PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    address VARCHAR(255),
    subscribing_newsletter BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

-- user_roles 中間テーブル
CREATE TABLE user_roles (
    user_id BIGINT NOT NULL,
    role_id INT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (role_id) REFERENCES roles(id)
);

-- categories テーブル
CREATE TABLE categories (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

-- products テーブル
CREATE TABLE products (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT(1000),
    image_url VARCHAR(255),
    price DECIMAL(10, 2) NOT NULL,
    category_id INT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    FOREIGN KEY (category_id) REFERENCES categories(id)
);

-- skus テーブル
CREATE TABLE skus (
    id INT AUTO_INCREMENT PRIMARY KEY,
    product_id INT NOT NULL,
    -- size と color の組み合わせでユニーク制約を設定
    -- nullだとユニーク制約が適用されないため、デフォルト値を設定
    size VARCHAR(100) NOT NULL DEFAULT 'N/A',
    color VARCHAR(100) NOT NULL DEFAULT 'N/A',
    extra_price DECIMAL(10, 2),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    FOREIGN KEY (product_id) REFERENCES products(id),
    -- 複合ユニーク制約
    UNIQUE (product_id, size, color)
);

-- inventory テーブル
CREATE TABLE inventories (
    id INT AUTO_INCREMENT PRIMARY KEY,
    sku_id INT NOT NULL UNIQUE,
    quantity INT NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    FOREIGN KEY (sku_id) REFERENCES skus(id)
);

-- cart テーブル
CREATE TABLE carts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    FOREIGN KEY (user_id) REFERENCES users(id)
)

-- cart_items テーブル
CREATE TABLE cart_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    cart_id BIGINT NOT NULL,
    sku_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    FOREIGN KEY (cart_id) REFERENCES carts(id),
    FOREIGN KEY (sku_id) REFERENCES skus(id),
    UNIQUE (cart_id, sku_id)
);