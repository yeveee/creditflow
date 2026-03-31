package com.nexusbank.creditflow.api.demande.modele;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.nexusbank.creditflow.commun.modele.ModeleApi;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(description = "Modèle API pour le changement de statut d'une demande")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChangementStatutApi implements ModeleApi{

    @NotBlank 
    private String statut;
    
}