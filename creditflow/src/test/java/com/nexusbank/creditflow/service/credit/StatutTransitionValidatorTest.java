package com.nexusbank.creditflow.service.credit;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import com.nexusbank.creditflow.service.credit.modele.StatutDemande;

public class StatutTransitionValidatorTest {
    
    private final StatutTransitionValidator validator = new StatutTransitionValidator();

    @Test
    void shouldAllowValidTransition() {
        assertDoesNotThrow(() -> validator.valider(StatutDemande.SOUMISE, StatutDemande.EN_INSTRUCTION));
    }

    @Test
    void shouldRejectInvalidTransition() {
        assertThrows(IllegalStateException.class, 
                () -> validator.valider(StatutDemande.SOUMISE, StatutDemande.APPROUVEE));
    }
}
