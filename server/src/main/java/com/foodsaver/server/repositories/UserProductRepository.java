package com.foodsaver.server.repositories;

import com.foodsaver.server.model.UserProduct;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserProductRepository extends JpaRepository<UserProduct, Long> {
}
