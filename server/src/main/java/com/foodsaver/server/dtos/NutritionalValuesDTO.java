package com.foodsaver.server.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
