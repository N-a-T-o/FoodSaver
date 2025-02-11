package com.foodsaver.server.exceptions.handler;

import com.foodsaver.server.dtos.response.ExceptionResponse;
import com.foodsaver.server.exceptions.AccessDeniedException;
import com.foodsaver.server.exceptions.ApiException;
import com.foodsaver.server.exceptions.ApiRequestException;
import com.foodsaver.server.exceptions.InvalidTokenException;
import com.foodsaver.server.exceptions.User.UsernamePasswordException;
import com.foodsaver.server.exceptions.ValidationException;
import com.foodsaver.server.utils.ApiExceptionParser;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.validation.ConstraintViolationException;
import lombok.AllArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


@RestControllerAdvice
@AllArgsConstructor
public class ApiExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ExceptionResponse> handleApiExceptions(ApiException exception) {
        ExceptionResponse apiException = ApiExceptionParser.parseException(exception);

        return ResponseEntity
                .status(apiException.getStatus())
                .body(apiException);
    }

    @ExceptionHandler(value = {ApiRequestException.class})
    public ResponseEntity<String> handleApiRequestException(ApiRequestException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<Object> handleTokenExpiredException() {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Token has expired");
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<String> handleAccessDeniedException(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex.getMessage());
    }

    @ExceptionHandler(MalformedJwtException.class)
    public ResponseEntity<String> handleGlobalException(MalformedJwtException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex.getMessage());
    }

    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<String> handleExpiredJwtException(ExpiredJwtException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex.getMessage());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<String> handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
        String errorMessage = ex.getCause().getCause().getMessage();
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorMessage);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ExceptionResponse> handleBadCredentialsExceptions() {
        return handleApiExceptions(new UsernamePasswordException());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ExceptionResponse> handleConstraintValidationExceptions(ConstraintViolationException exception) {
        return handleApiExceptions(new ValidationException(exception.getConstraintViolations()));
    }
}

