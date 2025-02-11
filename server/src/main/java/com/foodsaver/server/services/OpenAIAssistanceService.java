package com.foodsaver.server.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.foodsaver.server.dtos.response.ProductFromReceiptResponse;
import com.foodsaver.server.exceptions.AI.ErrorProcessingAIResponseException;
import com.foodsaver.server.exceptions.AI.UnableToExtractContentFromAIResponseException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;

@Service
public class OpenAIAssistanceService {
    private final RestTemplate restTemplate;
    private final static String openAIEndpoint = "https://api.openai.com/v1/chat/completions";
    private final String apiKey;
    private final ObjectMapper objectMapper;

    public OpenAIAssistanceService(RestTemplate restTemplate, @Value("${spring.ai.openai.api-key}") String apiKey, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.apiKey = apiKey;
        this.objectMapper = objectMapper;
    }

    public List<ProductFromReceiptResponse> analyzeReceipt(String base64Image) throws JsonProcessingException {
        String requestBody = buildReceiptRequestBody(base64Image);
        HttpEntity<String> requestEntity = createRequestEntity(requestBody);

        try {
            // Send the POST request to the OpenAI API.
            ResponseEntity<String> responseEntity = restTemplate.postForEntity(openAIEndpoint, requestEntity, String.class);
            String responseBody = responseEntity.getBody();

            if (responseBody != null) {
                String extractedContent = extractContent(responseBody);
                return parseProductList(extractedContent);
            } else {
                throw new UnableToExtractContentFromAIResponseException("Empty response from API");
            }
        } catch (Exception e) {
            throw new ErrorProcessingAIResponseException(e.getMessage());
        }
    }

    /**
     * Assembles the complete JSON request body as a String.
     */
    private String buildReceiptRequestBody(String base64Image) throws JsonProcessingException {
        String instructions = getReceiptTextInstructions();
        String imageUrl = "data:image/jpeg;base64," + base64Image;
        String userMessage = buildUserContent(instructions, imageUrl);
        return String.format("{\"model\": \"gpt-4o-mini\", \"messages\": [%s]}", userMessage);
    }

    /**
     * Returns the text instructions for receipt analysis.
     */
    private String getReceiptTextInstructions() {
        return "You are given a photo of a Bulgarian store receipt. " +
                "Please analyze the image and extract ONLY the edible (food) items. For each item, identify:\n\n" +
                "1. The product name (in Bulgarian or transliterated)(!!keep the product name as you see it, do not correct it!!),\n" +
                "2. The amount or quantity (0.5, 1, 2, 5...) and another field for unit it is measured (if available, e.g. \" kg\", \" бр.\", etc.)If you see many products with the same name write them as one product with same measuring unit while increasing the amount,\n" +
                "3. The price for single product and another field for the currency(write it as BGN, EUR, USD and so) (in the currency and format shown on the receipt).\n\n" +
                "Return these items in valid JSON format, using the structure - list of products, every product has field name, amount, unit, price, currency. You should fill the array correctly\n\n" +
                "**Important Requirements**:\n\n" +
                "- Exclude any non-food items (e.g., detergents, bags, etc.).\n" +
                "- If the receipt is not present or cannot give the desired information, or if you cannot identify any food products, return:\n" +
                "  Unable to scan products.\n" +
                "- Provide no other text, only the JSON output or the error message.";
    }

    /**
     * Builds the user message JSON object as a string. The content is an array of two items:
     * one text object and one image object.
     */
    private String buildUserContent(String textInstructions, String imageUrl) {
        String textMessage = String.format("{\"type\": \"text\", \"text\": \"%s\"}", escapeJson(textInstructions));
        String imageMessage = String.format("{\"type\": \"image_url\", \"image_url\": {\"url\": \"%s\"}}", escapeJson(imageUrl));
        String contentArray = String.format("[%s, %s]", textMessage, imageMessage);
        return String.format("{\"role\": \"user\", \"content\": %s}", contentArray);
    }

    /**
     * A simple helper to escape any quotes in the text. Here we use ObjectMapper to serialize
     * the string and then remove the surrounding quotes.
     */
    private String escapeJson(String text) {
        try {
            // Write the value as a JSON string (which adds quotes) and remove them.
            return objectMapper.writeValueAsString(text).replaceAll("^\"|\"$", "");
        } catch (JsonProcessingException e) {
            return text;
        }
    }

    /**
     * Creates the HttpEntity with the given request body and sets the required headers.
     */
    private HttpEntity<String> createRequestEntity(String requestBody) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiKey);
        return new HttpEntity<>(requestBody, headers);
    }

    /**
     * Extracts the "content" field from the API response.
     */
    public String extractContent(String aiGeneratedContent) {
        try {
            JsonNode rootNode = objectMapper.readTree(aiGeneratedContent);
            JsonNode messageContent = rootNode.path("choices").get(0).path("message").path("content");
            if (!messageContent.isMissingNode()) {
                return messageContent.asText();
            } else {
                throw new UnableToExtractContentFromAIResponseException("Unable to extract content from AI response");
            }
        } catch (Exception e) {
            throw new ErrorProcessingAIResponseException(e.getMessage());
        }
    }


    /**
     * Removes markdown formatting from the given JSON text.
     * For example, it removes any leading "```json" and trailing "```" if present.
     */
    private String stripMarkdownFormatting(String jsonText) {
        if (jsonText.startsWith("```json")) {
            jsonText = jsonText.substring("```json".length());
        }
        if (jsonText.endsWith("```")) {
            jsonText = jsonText.substring(0, jsonText.length() - 3);
        }
        return jsonText.trim();
    }

    /**
     * Parses the JSON (which might be an array or an object containing a "products" array)
     * into a List<ProductFromReceiptResponse>.
     */
    private List<ProductFromReceiptResponse> parseProductList(String json) {
        try {
            String cleanJson = stripMarkdownFormatting(json);

            // Check if the clean JSON starts with '[' (i.e. it is a JSON array)
            if (cleanJson.startsWith("[")) {
                return objectMapper.readValue(cleanJson,
                        new TypeReference<List<ProductFromReceiptResponse>>() {});
            } else {
                // Assume it's an object with a "products" field
                JsonNode rootNode = objectMapper.readTree(cleanJson);
                JsonNode productsNode = rootNode.path("products");
                if (!productsNode.isArray()) {
                    throw new ErrorProcessingAIResponseException("Invalid product JSON structure: 'products' is not an array.");
                }
                return objectMapper.readValue(productsNode.traverse(),
                        new TypeReference<List<ProductFromReceiptResponse>>() {});
            }
        } catch (IOException e) {
            throw new ErrorProcessingAIResponseException("Error parsing product list: " + e.getMessage());
        }
    }
}
