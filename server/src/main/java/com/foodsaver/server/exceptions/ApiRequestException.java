package com.foodsaver.server.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class ApiRequestException extends ApiException{
    public ApiRequestException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}

