package com.foodsaver.server.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
//@RequiredArgsConstructor
public class OpenAIAssistanceService {
    private final RestTemplate restTemplate;
    private final String openAIEndpoint = "https://api.openai.com/v1/chat/completions";

    //@Value("${spring.ai.openai.api-key}")
    private final String apiKey;
    private final ObjectMapper objectMapper;

    public OpenAIAssistanceService(RestTemplate restTemplate, @Value("${spring.ai.openai.api-key}") String apiKey, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.apiKey = apiKey;
        this.objectMapper = objectMapper;
    }

    public String analyzeReceipt(String base64Image) throws JsonProcessingException {
        // Build the text message node (with instructions)
        ObjectNode textMessageNode = objectMapper.createObjectNode();
        textMessageNode.put("type", "text");
        textMessageNode.put("text",
                "You are given a photo of a Bulgarian store receipt. " +
                        "Please analyze the image and extract ONLY the edible (food) items. For each item, identify:\n\n" +
                        "1. The product name (in Bulgarian or transliterated),\n" +
                        "2. The amount or quantity (if available, e.g. \"1 kg\", \"2 бр.\", etc.),\n" +
                        "3. The price (in the currency and format shown on the receipt).\n\n" +
                        "Return these items in valid JSON format, using the structure below:\n\n" +
                        "{\n" +
                        "  \"products\": [\n" +
                        "    {\n" +
                        "      \"name\": \"...\",\n" +
                        "      \"amount\": \"...\",\n" +
                        "      \"price\": \"...\"\n" +
                        "    },\n" +
                        "    ...\n" +
                        "  ]\n" +
                        "}\n\n" +
                        "**Important Requirements**:\n\n" +
                        "- Exclude any non-food items (e.g., detergents, bags, etc.).\n" +
                        "- If the receipt is not present or cannot give the desired information, or if you cannot identify any food products, return:\n" +
                        "  Unable to scan products.\n" +
                        "- Provide no other text, only the JSON output or the error message.");

        // Build the image message node.
        ObjectNode imageMessageNode = objectMapper.createObjectNode();
        imageMessageNode.put("type", "image_url");
        ObjectNode imageUrlNode = objectMapper.createObjectNode();
        // Prepend the proper prefix to indicate that this is a Base64-encoded JPEG image.
        imageUrlNode.put("url", "data:image/jpeg;base64," + base64Image);
        imageMessageNode.set("image_url", imageUrlNode);

        // Combine both messages into a content array.
        ArrayNode contentArray = objectMapper.createArrayNode();
        contentArray.add(textMessageNode);
        contentArray.add(imageMessageNode);

        // Create the single user message node that contains the content array.
        ObjectNode userMessageNode = objectMapper.createObjectNode();
        userMessageNode.put("role", "user");
        userMessageNode.set("content", contentArray);

        // Build the root JSON object for the request.
        ObjectNode rootNode = objectMapper.createObjectNode();
        // You can change the model if needed. The Python script used "gpt-4o-mini".
        rootNode.put("model", "gpt-4o-mini");
        ArrayNode messagesArray = objectMapper.createArrayNode();
        messagesArray.add(userMessageNode);
        rootNode.set("messages", messagesArray);

        // Convert the built JSON to a string.
        String requestBody = objectMapper.writeValueAsString(rootNode);

        // Set headers including authorization.
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiKey);
        HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, headers);

        try {
            // Send the POST request to the OpenAI API.
            ResponseEntity<String> responseEntity = restTemplate.postForEntity(openAIEndpoint, requestEntity, String.class);
            String responseBody = responseEntity.getBody();

            if (responseBody != null) {
                // Reuse the helper method to extract the "content" field from the response.
                return extractContent(responseBody);
            } else {
                throw new Exception();
                //throw new UnableToExtractContentFromAIResponseException(messageSource);
            }
        } catch (Exception e) {
            throw new RuntimeException();
            //throw new ErrorProcessingAIResponseException(messageSource);
        }
    }

    /**
     * Extracts the generated content from the AI response.
     * (This method is assumed to be already in place, as in your existing implementation.)
     */
    public String extractContent(String aiGeneratedContent) {
        try {
            JsonNode rootNode = objectMapper.readTree(aiGeneratedContent);
            JsonNode messageContent = rootNode.path("choices").get(0).path("message").path("content");
            if (!messageContent.isMissingNode()) {
                return messageContent.asText();
            } else {
                throw new Exception();
                //throw new UnableToExtractContentFromAIResponseException(messageSource);
            }
        } catch (Exception e) {
            throw new RuntimeException();
            //throw new ErrorProcessingAIResponseException(messageSource);
        }
    }
}
