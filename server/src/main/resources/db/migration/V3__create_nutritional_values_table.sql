CREATE TABLE nutritional_values (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    calories FLOAT CHECK (calories >= 0),
    protein FLOAT CHECK (protein >= 0),
    fat FLOAT CHECK (fat >= 0),
    carbohydrates FLOAT CHECK (carbohydrates >= 0),
    unit VARCHAR(50)
);
