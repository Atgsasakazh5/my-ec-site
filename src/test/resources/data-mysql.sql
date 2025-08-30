INSERT INTO roles (name) VALUES ('ROLE_USER');

-- usersテーブルにテストユーザーを1件挿入
INSERT INTO users
    (id, name, email, password, address, email_verified, subscribing_newsletter, created_at, updated_at)
VALUES
    (1, 'testuser', 'test@email.com', '$2a$10$DlinxRyxBS2JQbTVSYWKEOGxT5nJ7wZZd/MGpz72O4imXgGbLYdhC',
     '東京', true, true, '2023-10-01 12:00:00', '2023-10-01 12:00:00');

-- user_rolesテーブルで、user_id=1のユーザーにrole_id=1 (ROLE_USER) を紐付ける
INSERT INTO user_roles (user_id, role_id) VALUES (1, 1);