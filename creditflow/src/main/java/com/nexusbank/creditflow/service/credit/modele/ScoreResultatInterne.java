package com.nexusbank.creditflow.service.credit.modele;

import com.nexusbank.creditflow.commun.modele.ModeleInterne;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class ScoreResultatInterne implements ModeleInterne{
    
    private Integer score;
    private String risque;
    
}
