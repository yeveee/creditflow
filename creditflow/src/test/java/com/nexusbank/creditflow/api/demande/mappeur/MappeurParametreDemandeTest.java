package com.nexusbank.creditflow.api.demande.mappeur;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.nexusbank.creditflow.api.demande.modele.DemandeCreditApi;
import com.nexusbank.creditflow.service.credit.modele.DemandeCreditInterne;
import com.nexusbank.creditflow.service.credit.modele.StatutDemande;

@SpringBootTest
public class MappeurParametreDemandeTest {

    @Autowired
    private MappeurParametreDemande mapper;

    @Test
    void shouldMapApiToInterne() {

        // Given
        DemandeCreditApi api = DemandeCreditApi.builder()
                .montant(new BigDecimal("15000.00"))
                .dureeMois(24)
                .nomEmprunteur("Jean Dupont")
                .build();

        // When
        DemandeCreditInterne interne = mapper.map(api);

        // Then
        assertNotNull(interne);
        assertEquals(new BigDecimal("15000.00"), interne.getMontant().orElse(null));
        assertEquals(24, interne.getDureeMois().orElse(null));
        assertEquals("Jean Dupont", interne.getNomEmprunteur().orElse(null));
        assertEquals(StatutDemande.SOUMISE, interne.getStatut());
        assertFalse(interne.getId().isPresent());
        assertFalse(interne.getDateCreation().isPresent());
    }

    @Test
    void shouldWrapNullValuesInOptional() {
        // Given
        DemandeCreditApi api = DemandeCreditApi.builder()
                .montant(null)
                .dureeMois(null)
                .nomEmprunteur(null)
                .build();

        // When
        DemandeCreditInterne interne = mapper.map(api);

        // Then
        assertNotNull(interne);
        assertFalse(interne.getMontant().isPresent());
        assertFalse(interne.getDureeMois().isPresent());
        assertFalse(interne.getNomEmprunteur().isPresent());
    }
}
