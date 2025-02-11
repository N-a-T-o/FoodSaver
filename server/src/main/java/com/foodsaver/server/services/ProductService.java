package com.foodsaver.server.services;

import com.foodsaver.server.dtos.response.ProductFromReceiptResponse;
import com.foodsaver.server.enums.ReminderFrequency;
import com.foodsaver.server.model.Product;
import com.foodsaver.server.model.User;
import com.foodsaver.server.model.UserProduct;
import com.foodsaver.server.repositories.ProductRepository;
import com.foodsaver.server.repositories.UserProductRepository;
import com.foodsaver.server.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final UserProductRepository userProductRepository;
    private final UserRepository userRepository;

    public void saveNewProductsFromReceipt(List<ProductFromReceiptResponse> receiptResponseList) {
        for (ProductFromReceiptResponse product : receiptResponseList) {
            if (!productRepository.existsByName(product.getName())) {
                Product newProduct = Product.builder().name(product.getName()).unit(product.getUnit()).build();
                productRepository.save(newProduct);
            }
        }
    }

    public void saveNewUserProductsForUser(List<ProductFromReceiptResponse> receiptResponseList) {
        for (ProductFromReceiptResponse receiptProduct : receiptResponseList) {
            Optional<UserProduct> optionalUserProduct = userProductRepository.findByProduct_Name(receiptProduct.getName());
            if (optionalUserProduct.isPresent()) {
                UserProduct userProduct = optionalUserProduct.get();
                userProduct.setQuantity(userProduct.getQuantity() + receiptProduct.getAmount());
                userProductRepository.save(userProduct);
            } else {
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                User loggedUser = userRepository.findByUsername(authentication.getName());
                Product product = productRepository.findByName(receiptProduct.getName());
                UserProduct userProduct = UserProduct.builder()
                        .user(loggedUser)
                        .product(product)
                        .quantity(receiptProduct.getAmount())
                        .price(receiptProduct.getPrice())
                        .reminderFrequency(ReminderFrequency.ONCE_A_WEEK)
                        .createdAt(LocalDateTime.now())
                        .build();
                userProductRepository.save(userProduct);
            }
        }
    }
}
