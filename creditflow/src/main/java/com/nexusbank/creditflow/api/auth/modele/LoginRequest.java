package com.nexusbank.creditflow.api.auth.modele;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.nexusbank.creditflow.commun.modele.ModeleApi;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class LoginRequest implements ModeleApi {
    
    @NotBlank
    private String username;

    @NotBlank
    private String password;
}