package com.nexusbank.creditflow.service.credit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import com.nexusbank.creditflow.service.credit.modele.Role;

public class JwtServiceTest {
    
    private final JwtService jwtService = new JwtService("test-secret-key-qui-doit-etre-tres-longue-pour-HS384");

    @Test
    void shouldGenerateAndExtractUsername() {
        String token = jwtService.genererToken("jean", Role.ANALYSTE);
        assertEquals("jean", jwtService.extraireUsername(token));
    }

    @Test
    void shouldExtractRole() {
        String token = jwtService.genererToken("jean", Role.ANALYSTE);
        assertEquals("ANALYSTE", jwtService.extraireRole(token));
    }

    @Test
    void shouldValidateToken() {
        String token = jwtService.genererToken("jean", Role.ANALYSTE);
        assertTrue(jwtService.isTokenValide(token));
    }

    @Test
    void shouldRejectInvalidToken() {
        assertFalse(jwtService.isTokenValide("invalid-token"));
    }
}
