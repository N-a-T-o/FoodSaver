package com.foodsaver.server.exceptions.User;

import com.foodsaver.server.exceptions.ApiRequestException;
import jakarta.validation.ConstraintViolation;

import java.util.Set;
import java.util.stream.Collectors;

public class UserCreateException extends ApiRequestException {

    public UserCreateException(boolean isUnique) {
        super(
                isUnique
                        ? "User with the same email already exists!"
                        : "Invalid user data!"
        );
    }

    public UserCreateException(Set<ConstraintViolation<?>> validationErrors) {
        super(
                validationErrors
                        .stream()
                        .map(ConstraintViolation::getMessage)
                        .collect(Collectors.joining("\n"))
        );
    }
}
