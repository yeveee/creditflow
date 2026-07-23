package com.nexusbank.creditflow.api;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void shouldMapIllegalStateExceptionToConflict() {
        ResponseEntity<Map<String, Object>> response =
                handler.handleIllegalState(new IllegalStateException("Transition interdite : SOUMISE -> APPROUVEE"));

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("Conflict", response.getBody().get("error"));
        assertEquals("Transition interdite : SOUMISE -> APPROUVEE", response.getBody().get("message"));
    }

    @Test
    void shouldMapIllegalArgumentExceptionToBadRequest() {
        ResponseEntity<Map<String, Object>> response =
                handler.handleIllegalArgument(new IllegalArgumentException("Demande non trouvée : 999"));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Demande non trouvée : 999", response.getBody().get("message"));
    }

    @Test
    void shouldFallBackToReasonPhraseWhenExceptionHasNoMessage() {
        ResponseEntity<Map<String, Object>> response =
                handler.handleIllegalState(new IllegalStateException());

        assertEquals("Conflict", response.getBody().get("message"));
    }
}
