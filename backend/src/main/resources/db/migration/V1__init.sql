-- ============================================================
-- Flash-sale: схема v1
-- ============================================================

-- Пользователи: покупатели + админы
CREATE TABLE users (
                       id            BIGSERIAL PRIMARY KEY,
                       email         VARCHAR(255) NOT NULL UNIQUE,
                       password_hash VARCHAR(255) NOT NULL,
                       role          VARCHAR(20)  NOT NULL,
                       created_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
                       CONSTRAINT chk_users_role CHECK (role IN ('BUYER', 'ADMIN'))
);

-- Товары каталога (принадлежат магазину, продавец один)
CREATE TABLE products (
                          id          BIGSERIAL PRIMARY KEY,
                          name        VARCHAR(255)   NOT NULL,
                          description TEXT,
                          image_url   VARCHAR(500),
                          price       NUMERIC(10, 2) NOT NULL,
                          CONSTRAINT chk_products_price CHECK (price >= 0)
);

-- Распродажи: событие над товаром (цена, сток, окно времени)
CREATE TABLE flash_sales (
                             id          BIGSERIAL PRIMARY KEY,
                             product_id  BIGINT         NOT NULL REFERENCES products (id) ON DELETE RESTRICT,
                             sale_price  NUMERIC(10, 2) NOT NULL,
                             total_stock INT            NOT NULL,
                             starts_at   TIMESTAMPTZ    NOT NULL,
                             ends_at     TIMESTAMPTZ    NOT NULL,
                             status      VARCHAR(20)    NOT NULL,
                             created_by  BIGINT         NOT NULL REFERENCES users (id) ON DELETE RESTRICT,
                             CONSTRAINT chk_sales_price  CHECK (sale_price >= 0),
                             CONSTRAINT chk_sales_stock  CHECK (total_stock > 0),
                             CONSTRAINT chk_sales_window CHECK (ends_at > starts_at),
                             CONSTRAINT chk_sales_status CHECK (status IN ('DRAFT', 'SCHEDULED', 'LIVE', 'FINISHED'))
);

-- Заказы: главный ledger. unique-ограничения здесь — бизнес-правила,
-- которые нельзя обойти никаким багом в коде.
CREATE TABLE orders (
                        id         BIGSERIAL PRIMARY KEY,
                        order_no   VARCHAR(64)    NOT NULL UNIQUE,
                        user_id    BIGINT         NOT NULL REFERENCES users (id) ON DELETE RESTRICT,
                        sale_id    BIGINT         NOT NULL REFERENCES flash_sales (id) ON DELETE RESTRICT,
                        amount     NUMERIC(10, 2) NOT NULL,
                        status     VARCHAR(20)    NOT NULL,
                        created_at TIMESTAMPTZ    NOT NULL DEFAULT now(),
                        paid_at    TIMESTAMPTZ,
                        expires_at TIMESTAMPTZ    NOT NULL,
                        CONSTRAINT chk_orders_amount CHECK (amount >= 0),
                        CONSTRAINT chk_orders_status CHECK (status IN ('PENDING_PAYMENT', 'PAID', 'EXPIRED', 'CANCELLED', 'REJECTED'))
);

-- «1 товар в одни руки» + идемпотентность консьюмера Kafka
CREATE UNIQUE INDEX uk_orders_user_sale ON orders (user_id, sale_id);

-- Дашборд админа: сколько продано в конкретной распродаже
CREATE INDEX ix_orders_sale ON orders (sale_id);