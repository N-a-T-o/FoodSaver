package com.foodsaver.server.utils;

import com.foodsaver.server.dtos.response.ExceptionResponse;
import com.foodsaver.server.exceptions.ApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;

class ApiExceptionParserTest {

    @Mock
    private ApiException apiException;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void parseException_ShouldReturnValidExceptionResponse() {
        String expectedMessage = "An error occurred";
        HttpStatus expectedStatus = HttpStatus.BAD_REQUEST;
        int expectedStatusCode = expectedStatus.value();
        LocalDateTime now = LocalDateTime.now();

        when(apiException.getMessage()).thenReturn(expectedMessage);
        when(apiException.getStatus()).thenReturn(expectedStatus);
        when(apiException.getStatusCode()).thenReturn(expectedStatusCode);

        ExceptionResponse response = ApiExceptionParser.parseException(apiException);

        assertEquals(expectedMessage, response.getMessage());
        assertEquals(expectedStatus, response.getStatus());
        assertEquals(expectedStatusCode, response.getStatusCode());
        assertEquals(now.getDayOfMonth(), response.getDateTime().getDayOfMonth());
        assertEquals(now.getHour(), response.getDateTime().getHour()); // Ensure timestamps are close
    }
}
