package com.nexusbank.creditflow.service.credit;

import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Component;

import com.nexusbank.creditflow.service.credit.modele.Role;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component

public class JwtService {
    
    private static final String SECRET = "creditflow-secret-key-qui-doit-etre-tres-longue-pour-HS256";
    private static final long EXPIRATION_MS = 86400000; // 24 hours
    
    private final SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes());

    public String genererToken(String username, Role role) {
    return Jwts.builder()
            .subject(username)                    
            .claim("role", role.name())           
            .issuedAt(new Date())                 
            .expiration(new Date(                 
                System.currentTimeMillis() + EXPIRATION_MS))
            .signWith(key)                        
            .compact();                           
}

private Claims extraireClaims(String token) {
    return Jwts.parser()
            .verifyWith(key)              
            .build()
            .parseSignedClaims(token)     
            .getPayload();                
}

public String extraireUsername(String token) {
    return extraireClaims(token).getSubject();  
}

public String extraireRole(String token) {
    return extraireClaims(token).get("role", String.class);  
}

public boolean isTokenValide(String token) {
    try {
        extraireClaims(token);  
        return true;            
    } catch (Exception e) {
        return false;           
    }
}
}
