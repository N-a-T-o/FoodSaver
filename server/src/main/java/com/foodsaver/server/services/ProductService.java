package com.foodsaver.server.services;

import com.foodsaver.server.repositories.ProductRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
//@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
}
