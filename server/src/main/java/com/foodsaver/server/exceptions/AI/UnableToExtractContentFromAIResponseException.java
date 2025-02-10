package com.foodsaver.server.exceptions.AI;

import com.foodsaver.server.exceptions.ApiRequestException;

public class UnableToExtractContentFromAIResponseException extends ApiRequestException {
    public UnableToExtractContentFromAIResponseException(String message) {
        super(message);
    }
}
