package com.foodsaver.server.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductFromReceiptResponse {
    private String name;
    private float amount;
    private String unit;
    private Float price;
    private String currency;
}
