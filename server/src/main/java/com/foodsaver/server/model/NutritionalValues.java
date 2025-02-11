package com.foodsaver.server.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "nutritional_values")
public class NutritionalValues {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @OneToOne(mappedBy = "nutritionalValues")
    private Product product;

    @Min(value = 0, message = "Calories can not be less than 0!")
    private Float calories;

    @Min(value = 0, message = "Protein can not be less than 0!")
    private Float protein;

    @Min(value = 0, message = "Fat can not be less than 0!")
    private Float fat;

    @Min(value = 0, message = "Carbohydrates can not be less than 0!")
    private Float carbohydrates;

    @Length(max = 50, message = "Unit can not be more than 50 characters long!")
    private String unit;
}
