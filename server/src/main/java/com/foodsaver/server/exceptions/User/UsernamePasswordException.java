package com.foodsaver.server.exceptions.User;

import com.foodsaver.server.exceptions.ApiException;
import org.springframework.http.HttpStatus;

public class UsernamePasswordException extends ApiException {
    public UsernamePasswordException() {
        super("Invalid email or password", HttpStatus.UNAUTHORIZED);
    }
}
