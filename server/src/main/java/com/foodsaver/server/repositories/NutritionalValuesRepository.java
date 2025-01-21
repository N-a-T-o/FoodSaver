package com.foodsaver.server.repositories;

import com.foodsaver.server.model.NutritionalValues;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NutritionalValuesRepository extends JpaRepository<NutritionalValues, Long> {
}
