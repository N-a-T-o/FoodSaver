package com.foodsaver.server.dtos;

import com.foodsaver.server.model.Product;
import jakarta.persistence.OneToOne;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NutritionalValuesDTO {
    private Long id;

    private ProductDTO productDTO;

    private Float calories;

    private Float protein;

    private Float fat;

    private Float carbohydrates;

    private String unit;
}
