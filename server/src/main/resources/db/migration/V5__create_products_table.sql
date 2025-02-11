CREATE TABLE products (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    barcode VARCHAR(50) UNIQUE,
    name VARCHAR(255) NOT NULL,
    brand VARCHAR(50),
    unit VARCHAR(255),
    nutritional_values_id BIGINT,
    created_at DATETIME,
    deleted_at DATETIME,
    CONSTRAINT fk_nutritional_values FOREIGN KEY (nutritional_values_id) REFERENCES nutritional_values(id)
);
