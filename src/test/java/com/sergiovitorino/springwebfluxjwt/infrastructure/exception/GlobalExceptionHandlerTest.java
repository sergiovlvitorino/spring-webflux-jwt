package com.sergiovitorino.springwebfluxjwt.infrastructure.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void testHandleIllegalArgument() {
        var response = handler.handleIllegalArgument(new IllegalArgumentException("test error"));
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("test error", response.getBody().get("error"));
    }

    @Test
    void testHandleGenericException() {
        var response = handler.handleGenericException(new RuntimeException("unexpected"));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Internal server error", response.getBody().get("error"));
    }
}
