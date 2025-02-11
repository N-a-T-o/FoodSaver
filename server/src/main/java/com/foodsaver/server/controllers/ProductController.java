package com.foodsaver.server.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.foodsaver.server.dtos.response.ProductFromReceiptResponse;
import com.foodsaver.server.repositories.ProductRepository;
import com.foodsaver.server.services.OpenAIAssistanceService;
import com.foodsaver.server.services.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/products")
public class ProductController {
    private final OpenAIAssistanceService aiAssistanceService;
    private final ProductService productService;
    private final ProductRepository productRepository;

    @PostMapping("/scan-receipt")
    public ResponseEntity<List<ProductFromReceiptResponse>> analyzeReceipt(@RequestBody Map<String, String> requestBody) throws JsonProcessingException {
        // Expect the request body to have a key "base64Image"
        String base64Image = requestBody.get("base64Image");
        List<ProductFromReceiptResponse> analysisResult = aiAssistanceService.analyzeReceipt(base64Image);
        return ResponseEntity.ok(analysisResult);
    }
}

