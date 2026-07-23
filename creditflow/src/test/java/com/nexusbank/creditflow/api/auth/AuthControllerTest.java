package com.nexusbank.creditflow.api.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.nexusbank.creditflow.api.auth.modele.LoginRequest;
import com.nexusbank.creditflow.isolation.db.UtilisateurRepository;
import com.nexusbank.creditflow.isolation.db.modele.UtilisateurEntity;
import com.nexusbank.creditflow.service.credit.JwtService;
import com.nexusbank.creditflow.service.credit.modele.Role;

@ExtendWith(MockitoExtension.class)
public class AuthControllerTest {

    @Mock private UtilisateurRepository utilisateurRepository;
    @Mock private JwtService jwtService;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthController controller;

    @Test
    void shouldReturnTokenForValidCredentials() {
        UtilisateurEntity user = UtilisateurEntity.builder()
                .id(1L).username("jean").password("hash").role(Role.ANALYSTE).build();
        when(utilisateurRepository.findByUsername("jean")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("secret", "hash")).thenReturn(true);
        when(jwtService.genererToken("jean", Role.ANALYSTE)).thenReturn("jwt-token");

        LoginRequest request = LoginRequest.builder().username("jean").password("secret").build();
        ResponseEntity<?> response = controller.login(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(Map.of("token", "jwt-token"), response.getBody());
    }

    @Test
    void shouldReturn401ForUnknownUser() {
        when(utilisateurRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        LoginRequest request = LoginRequest.builder().username("ghost").password("x").build();
        ResponseEntity<?> response = controller.login(request);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals(Map.of("error", "Identifiants invalides"), response.getBody());
    }

    @Test
    void shouldReturn401ForWrongPassword() {
        UtilisateurEntity user = UtilisateurEntity.builder()
                .id(1L).username("jean").password("hash").role(Role.CLIENT).build();
        when(utilisateurRepository.findByUsername("jean")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        LoginRequest request = LoginRequest.builder().username("jean").password("wrong").build();
        ResponseEntity<?> response = controller.login(request);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }
}
