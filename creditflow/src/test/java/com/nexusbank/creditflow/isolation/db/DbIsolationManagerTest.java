package com.nexusbank.creditflow.isolation.db;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.nexusbank.creditflow.commun.mappeur.MappeurUtils;
import com.nexusbank.creditflow.isolation.db.modele.DemandeCreditEntity;
import com.nexusbank.creditflow.service.credit.modele.DemandeCreditInterne;
import com.nexusbank.creditflow.service.credit.modele.StatutDemande;

@ExtendWith(MockitoExtension.class)
public class DbIsolationManagerTest {

    @Mock private DemandeCreditRepository repository;
    @Mock private DecisionAuditRepository auditRepository;

    private DbIsolationManager manager() {
        return new DbIsolationManager(repository, auditRepository, new MappeurUtils());
    }

    private DemandeCreditEntity entity(Long id) {
        return DemandeCreditEntity.builder()
                .id(id)
                .montant(new BigDecimal("15000.00"))
                .dureeMois(24)
                .nomEmprunteur("Jean Dupont")
                .statut("SOUMISE")
                .dateCreation(LocalDateTime.now())
                .clientUsername("client1")
                .build();
    }

    private DemandeCreditInterne interne() {
        return DemandeCreditInterne.builder()
                .id(Optional.empty())
                .montant(Optional.of(new BigDecimal("15000.00")))
                .dureeMois(Optional.of(24))
                .nomEmprunteur(Optional.of("Jean Dupont"))
                .statut(StatutDemande.SOUMISE)
                .dateCreation(Optional.empty())
                .scoreCredit(Optional.empty())
                .risqueCredit(Optional.empty())
                .clientUsername(Optional.of("client1"))
                .build();
    }

    @Test
    void shouldSaveAndMapBackToInterne() {
        when(repository.save(any())).thenReturn(entity(1L));

        DemandeCreditInterne result = manager().save(interne());

        assertEquals(Optional.of(1L), result.getId());
        assertEquals(Optional.of("client1"), result.getClientUsername());
        assertEquals(Optional.of("Jean Dupont"), result.getNomEmprunteur());
    }

    @Test
    void shouldFindById() {
        when(repository.findById(1L)).thenReturn(Optional.of(entity(1L)));

        Optional<DemandeCreditInterne> result = manager().findById(1L);

        assertTrue(result.isPresent());
        assertEquals(Optional.of(1L), result.get().getId());
    }

    @Test
    void shouldReturnEmptyWhenNotFoundById() {
        when(repository.findById(999L)).thenReturn(Optional.empty());

        assertTrue(manager().findById(999L).isEmpty());
    }

    @Test
    void shouldFindAll() {
        when(repository.findAll()).thenReturn(List.of(entity(1L), entity(2L)));

        List<DemandeCreditInterne> result = manager().findAll();

        assertEquals(2, result.size());
    }

    @Test
    void shouldFindAllPaginated() {
        Page<DemandeCreditEntity> page = new PageImpl<>(List.of(entity(1L)));
        when(repository.findAll(any(Pageable.class))).thenReturn(page);

        Page<DemandeCreditInterne> result = manager().findAll(Pageable.unpaged());

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void shouldUpdateStatut() {
        DemandeCreditEntity existing = entity(1L);
        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.save(any())).thenReturn(existing);

        Optional<DemandeCreditInterne> result = manager().updateStatut(1L, "EN_INSTRUCTION");

        assertTrue(result.isPresent());
        verify(repository).save(argThat(e -> "EN_INSTRUCTION".equals(e.getStatut())));
    }

    @Test
    void shouldReturnEmptyWhenUpdatingStatutOfMissingDemande() {
        when(repository.findById(999L)).thenReturn(Optional.empty());

        assertTrue(manager().updateStatut(999L, "EN_INSTRUCTION").isEmpty());
    }

    @Test
    void shouldSaveAuditEntry() {
        manager().auditerChangementStatut(1L, "EN_INSTRUCTION", "analyste1");

        verify(auditRepository).save(argThat(audit ->
                audit.getDemandeId().equals(1L)
                        && "EN_INSTRUCTION".equals(audit.getNouveauStatut())
                        && "analyste1".equals(audit.getModifiePar())
                        && audit.getDateModification() != null));
    }
}
