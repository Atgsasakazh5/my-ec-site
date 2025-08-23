INSERT INTO roles (name) VALUES ('ROLE_USER');
INSERT INTO roles (name) VALUES ('ROLE_ADMIN');

-- usersテーブルにテストユーザーを1件挿入
INSERT INTO users
    (id, name, email, password, address, subscribing_newsletter, created_at, updated_at)
VALUES
    (1, 'testuser', 'test@email.com', '$2a$10$DlinxRyxBS2JQbTVSYWKEOGxT5nJ7wZZd/MGpz72O4imXgGbLYdhC',
     '東京', true, '2023-10-01 12:00:00', '2023-10-01 12:00:00');

INSERT INTO users
    (id, name, email, password, address, subscribing_newsletter, created_at, updated_at)
VALUES
    (2, 'admin', 'admin@email.com', '$2a$10$pWG6wyXFmoQNBV19LSHbN.1WRIranCc5Xgr/w.oyvSieQITsCZ80C',
     '東京', true, '2023-10-01 12:00:00', '2023-10-01 12:00:00');

-- user_rolesテーブルで、user_id=1のユーザーにrole_id=1 (ROLE_USER) を紐付ける
INSERT INTO user_roles (user_id, role_id) VALUES (1, 1);
INSERT INTO user_roles (user_id, role_id) VALUES (2, 2); -- adminにROLE_ADMINを付与

-- カテゴリデータ
INSERT INTO categories (id, name) VALUES (1, 'トップス'), (2, 'ボトムス');

-- 商品データ
INSERT INTO products (id, name, price, description, category_id, created_at, updated_at) VALUES
(1, '高機能Tシャツ', 1500, '夏に最適なTシャツです。', 1, NOW(), NOW()),
(2, 'クールジーンズ', 3000, '涼しい素材のジーンズです。', 2, NOW(), NOW());

-- SKUと在庫データ
INSERT INTO skus (id, product_id, size, color, extra_price, created_at, updated_at) VALUES
(101, 1, 'M', 'White', 0, NOW(), NOW()),
(102, 1, 'L', 'Black', 0, NOW(), NOW()),
(103, 2, '28', 'Indigo', 0, NOW(), NOW());

INSERT INTO inventories (sku_id, quantity, updated_at) VALUES
(101, 50, NOW()),
(102, 30, NOW()),
(103, 40, NOW());