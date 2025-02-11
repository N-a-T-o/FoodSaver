package com.foodsaver.server.exceptions;

public class InvalidTokenException extends UnauthorizedException {
    public InvalidTokenException(String message) {
        super(message);
    }
}

