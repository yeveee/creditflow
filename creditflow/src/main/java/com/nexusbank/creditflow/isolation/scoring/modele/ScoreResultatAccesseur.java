package com.nexusbank.creditflow.isolation.scoring.modele;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.nexusbank.creditflow.commun.modele.ModeleAccesseur;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class ScoreResultatAccesseur implements ModeleAccesseur {
    
    private Integer score;
    private String risque;
}