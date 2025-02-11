package com.foodsaver.server.exceptions.AI;

import com.foodsaver.server.exceptions.ApiRequestException;

public class ErrorProcessingAIResponseException extends ApiRequestException {
    public ErrorProcessingAIResponseException(String message) {
        super(message);
    }
}
