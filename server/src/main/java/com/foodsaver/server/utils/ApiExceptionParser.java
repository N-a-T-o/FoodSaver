package com.foodsaver.server.utils;

import com.foodsaver.server.dtos.response.ExceptionResponse;
import com.foodsaver.server.exceptions.ApiException;

import java.time.LocalDateTime;

public class ApiExceptionParser {
    public static ExceptionResponse parseException(ApiException exception) {
        return ExceptionResponse
                .builder()
                .dateTime(LocalDateTime.now())
                .message(exception.getMessage())
                .status(exception.getStatus())
                .statusCode(exception.getStatusCode())
                .build();
    }
}