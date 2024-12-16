package com.foodsaver.server.exceptions;

public class UsernamePasswordException extends RuntimeException{
    public UsernamePasswordException(String message) {
        super(message);
    }
}
