package com.nexusbank.creditflow.service.credit;

import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.nexusbank.creditflow.service.credit.modele.StatutDemande;
@Component
public class StatutTransitionValidator {
    
    private static final Map<StatutDemande, Set<StatutDemande>> transitions = Map.of(
    StatutDemande.SOUMISE, Set.of(StatutDemande.EN_INSTRUCTION),

    StatutDemande.EN_INSTRUCTION, Set.of(StatutDemande.SCORING_EN_COURS),

    StatutDemande.SCORING_EN_COURS, Set.of(
        StatutDemande.APPROUVEE, 
        StatutDemande.REFUSEE, 
        StatutDemande.EN_ATTENTE_PIECES
    ),

    StatutDemande.EN_ATTENTE_PIECES, Set.of(StatutDemande.EN_INSTRUCTION)
    );

    public void valider(StatutDemande actuel, StatutDemande cible) {

    Set<StatutDemande> ciblesAutorisees = transitions.get(actuel);

    if (ciblesAutorisees == null || !ciblesAutorisees.contains(cible)) {
        throw new IllegalStateException(
            "Transition interdite : " + actuel + " → " + cible
        );
    }
}
}