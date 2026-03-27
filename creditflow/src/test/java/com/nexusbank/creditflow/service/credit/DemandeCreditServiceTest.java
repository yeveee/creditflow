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
import com.nexusbank.creditflow.service.credit.modele.DemandeCreditInterne;
import com.nexusbank.creditflow.service.credit.modele.StatutDemande;

@ExtendWith(MockitoExtension.class)
public class DemandeCreditServiceTest {

    @Mock
    private DbIsolationManager dbIsolationManager;
 
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

            when(dbIsolationManager.save(any(DemandeCreditInterne.class)))
            .thenReturn(saved);

            // When
    DemandeCreditInterne result = service.creerDemande(demandeTest);

    // Then
    assertNotNull(result);
    assertEquals(StatutDemande.SOUMISE, result.getStatut());
    assertEquals(1L, result.getId().orElse(null));
    verify(dbIsolationManager, times(1)).save(any(DemandeCreditInterne.class));
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


    
}
