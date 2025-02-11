CREATE TABLE user_products (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity FLOAT CHECK (quantity >= 0),
    price FLOAT CHECK (price >= 0),
    reminder_frequency VARCHAR(255),
    expiration_date DATETIME,
    created_at DATETIME,
    CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_product FOREIGN KEY (product_id) REFERENCES products(id)
);
