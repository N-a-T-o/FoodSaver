package com.foodsaver.server.repositories;

import com.foodsaver.server.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
    boolean existsByName(String name);

    Product findByName(String name);
}