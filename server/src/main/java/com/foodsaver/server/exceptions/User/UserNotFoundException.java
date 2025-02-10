package com.foodsaver.server.exceptions.User;

import com.foodsaver.server.exceptions.ApiException;
import org.springframework.http.HttpStatus;

public class UserNotFoundException extends ApiException {
    public UserNotFoundException() {
        super("User not found", HttpStatus.NOT_FOUND);
    }

    public UserNotFoundException(String field) {
        super(field, HttpStatus.NOT_FOUND);
    }
}
