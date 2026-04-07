package com.nexusbank.creditflow.api.auth;

import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nexusbank.creditflow.api.auth.modele.LoginRequest;
import com.nexusbank.creditflow.isolation.db.UtilisateurRepository;
import com.nexusbank.creditflow.isolation.db.modele.UtilisateurEntity;
import com.nexusbank.creditflow.service.credit.JwtService;

import jakarta.validation.Valid;
import java.util.Map;
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    
    private final UtilisateurRepository utilisateurRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    public AuthController(UtilisateurRepository utilisateurRepository,
                          JwtService jwtService,
                          PasswordEncoder passwordEncoder) {
        this.utilisateurRepository = utilisateurRepository;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
    }
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        
        UtilisateurEntity user = utilisateurRepository
                .findByUsername(request.getUsername())
                .orElse(null);
 
        
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return ResponseEntity.status(401).body(Map.of("error", "Identifiants invalides"));
        }
 
        
        String token = jwtService.genererToken(user.getUsername(), user.getRole());
 
        
        return ResponseEntity.ok(Map.of("token", token));
    }

}
