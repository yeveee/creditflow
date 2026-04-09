package com.nexusbank.creditflow.service.credit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.nexusbank.creditflow.isolation.db.DbIsolationManager;
import com.nexusbank.creditflow.isolation.scoring.ScoringIsolationManager;
import com.nexusbank.creditflow.service.credit.modele.DemandeCreditInterne;
import com.nexusbank.creditflow.service.credit.modele.ScoreResultatInterne;
import com.nexusbank.creditflow.service.credit.modele.StatutDemande;

@ExtendWith(MockitoExtension.class)
public class DemandeCreditServiceTest {

    @Mock
    private DbIsolationManager dbIsolationManager;

    @Mock
    private ScoringIsolationManager scoringIsolationManager;

    @Mock
    private StatutTransitionValidator statutTransitionValidator;

    @Mock
    private NotificationPublisher notificationPublisher;

    @InjectMocks
    private DemandeCreditService service;
 
    private DemandeCreditInterne demandeTest;

    @BeforeEach
void setUp() {
    demandeTest = DemandeCreditInterne.builder()
            .id(Optional.empty())
            .montant(Optional.of(new BigDecimal("20000.00")))
            .dureeMois(Optional.of(36))
            .nomEmprunteur(Optional.of("Marie Martin"))
            .statut(StatutDemande.SOUMISE)
            .dateCreation(Optional.empty())
            .build();
}

@Test
void shouldCreateDemandeWithStatusSoumise() {

    // Given
    DemandeCreditInterne saved = demandeTest.toBuilder()
            .id(Optional.of(1L))
            .build();

    DemandeCreditInterne avecScore = saved.toBuilder()
            .scoreCredit(Optional.of(750))
            .risqueCredit(Optional.of("FAIBLE"))
            .build(); 

    when(dbIsolationManager.save(any(DemandeCreditInterne.class)))
            .thenReturn(saved)
            .thenReturn(avecScore);

    when(scoringIsolationManager.calculerScore("Marie Martin"))
            .thenReturn(new ScoreResultatInterne(750, "FAIBLE"));

    // When
    DemandeCreditInterne result = service.creerDemande(demandeTest);

        // Then
    assertNotNull(result);
    assertEquals(750, result.getScoreCredit().orElse(null));
    verify(dbIsolationManager, times(2)).save(any());
    verify(scoringIsolationManager).calculerScore("Marie Martin");
}

@Test
void shouldFindDemandeById() {

    // Given
    Long id = 1L;
    when(dbIsolationManager.findById(id))
            .thenReturn(Optional.of(demandeTest));

    Optional<DemandeCreditInterne> result = service.obtenirDemande(id);

    assertTrue(result.isPresent());
    assertEquals("Marie Martin", result.get().getNomEmprunteur().orElse(null));
    verify(dbIsolationManager, times(1)).findById(id);
            
}

@Test
void shouldReturnEmptyWhenDemandeNotFound() {

    // Given
    Long id = 999L;
    when(dbIsolationManager.findById(id))
            .thenReturn(Optional.empty());

    Optional<DemandeCreditInterne> result = service.obtenirDemande(999L);

    assertFalse(result.isPresent());
    verify(dbIsolationManager, times(1)).findById(id);
}

@Test
void shouldChangeStatutSuccessfully() {

    // Given
    Long id = 1L;
    DemandeCreditInterne demande = demandeTest.toBuilder()
            .id(Optional.of(id))
            .statut(StatutDemande.SOUMISE)
            .build();

    DemandeCreditInterne updated = demande.toBuilder()
            .statut(StatutDemande.EN_INSTRUCTION)
            .build();

    when(dbIsolationManager.findById(id)).thenReturn(Optional.of(demande));
    when(dbIsolationManager.updateStatut(id, "EN_INSTRUCTION")).thenReturn(Optional.of(updated));

    // When
    Optional<DemandeCreditInterne> result = service.changerStatut(id, StatutDemande.EN_INSTRUCTION);
 
        // Then
    assertTrue(result.isPresent());
    assertEquals(StatutDemande.EN_INSTRUCTION, result.get().getStatut());
    verify(statutTransitionValidator).valider(StatutDemande.SOUMISE, StatutDemande.EN_INSTRUCTION);
    verify(notificationPublisher).publierChangementStatut(id, "EN_INSTRUCTION");
 
}


    
}
