package com.foodsaver.server.repositories;

import com.foodsaver.server.model.UserProduct;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserProductRepository extends JpaRepository<UserProduct, Long> {
    Optional<UserProduct> findByProduct_Name(String productName);
}
