package com.nexusbank.creditflow.api.demande;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.nexusbank.creditflow.api.demande.modele.ChangementStatutApi;
import com.nexusbank.creditflow.api.demande.modele.DemandeCreditApi;
import com.nexusbank.creditflow.commun.mappeur.MappeurUtils;
import com.nexusbank.creditflow.service.credit.DemandeCreditService;
import com.nexusbank.creditflow.service.credit.modele.DemandeCreditInterne;
import com.nexusbank.creditflow.service.credit.modele.StatutDemande;

@ExtendWith(MockitoExtension.class)
public class DemandeCreditControllerTest {

    @Mock private DemandeCreditService service;
    @Mock private Authentication authentication;

    private DemandeCreditController newController() {
        return new DemandeCreditController(service, new MappeurUtils());
    }

    private DemandeCreditInterne demande(Long id, String clientUsername) {
        return DemandeCreditInterne.builder()
                .id(Optional.ofNullable(id))
                .montant(Optional.of(new BigDecimal("15000.00")))
                .dureeMois(Optional.of(24))
                .nomEmprunteur(Optional.of("Jean Dupont"))
                .statut(StatutDemande.SOUMISE)
                .dateCreation(Optional.of(LocalDateTime.now()))
                .scoreCredit(Optional.of(500))
                .risqueCredit(Optional.of("MOYEN"))
                .clientUsername(Optional.ofNullable(clientUsername))
                .build();
    }

    @Test
    void shouldCreateDemandeAndAttachAuthenticatedUsername() {
        DemandeCreditController controller = newController();
        when(authentication.getName()).thenReturn("client1");
        when(service.creerDemande(any())).thenAnswer(inv -> {
            DemandeCreditInterne arg = inv.getArgument(0);
            assertEquals(Optional.of("client1"), arg.getClientUsername());
            return demande(1L, "client1");
        });

        DemandeCreditApi request = DemandeCreditApi.builder()
                .montant(new BigDecimal("15000.00"))
                .dureeMois(24)
                .nomEmprunteur("Jean Dupont")
                .build();

        ResponseEntity<DemandeCreditApi> response = controller.creerDemande(request, authentication);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(1L, response.getBody().getId());
    }

    @Test
    void ownerCanViewTheirOwnDemande() {
        DemandeCreditController controller = newController();
        when(service.obtenirDemande(1L)).thenReturn(Optional.of(demande(1L, "client1")));
        when(authentication.getName()).thenReturn("client1");
        doReturn(List.of(new SimpleGrantedAuthority("ROLE_CLIENT"))).when(authentication).getAuthorities();

        ResponseEntity<DemandeCreditApi> response = controller.obtenirDemande(1L, authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void otherClientCannotViewSomeoneElsesDemande() {
        DemandeCreditController controller = newController();
        when(service.obtenirDemande(1L)).thenReturn(Optional.of(demande(1L, "client1")));
        when(authentication.getName()).thenReturn("client2");
        doReturn(List.of(new SimpleGrantedAuthority("ROLE_CLIENT"))).when(authentication).getAuthorities();

        assertThrows(AccessDeniedException.class, () -> controller.obtenirDemande(1L, authentication));
    }

    @Test
    void analysteCanViewAnyDemandeRegardlessOfOwnership() {
        DemandeCreditController controller = newController();
        when(service.obtenirDemande(1L)).thenReturn(Optional.of(demande(1L, "client1")));
        doReturn(List.of(new SimpleGrantedAuthority("ROLE_ANALYSTE"))).when(authentication).getAuthorities();

        ResponseEntity<DemandeCreditApi> response = controller.obtenirDemande(1L, authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void shouldReturn404WhenDemandeNotFoundOnGet() {
        DemandeCreditController controller = newController();
        when(service.obtenirDemande(999L)).thenReturn(Optional.empty());

        ResponseEntity<DemandeCreditApi> response = controller.obtenirDemande(999L, authentication);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void shouldListPaginatedDemandesForStaff() {
        DemandeCreditController controller = newController();
        Page<DemandeCreditInterne> page = new PageImpl<>(List.of(demande(1L, "client1")));
        doReturn(List.of(new SimpleGrantedAuthority("ROLE_ANALYSTE"))).when(authentication).getAuthorities();
        when(service.obtenirToutesPaginees(any())).thenReturn(page);

        ResponseEntity<Page<DemandeCreditApi>> response =
                controller.obtenirToutesLesDemandes(Pageable.unpaged(), authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().getTotalElements());
    }

    @Test
    void shouldScopeListToOwnDemandesForClient() {
        DemandeCreditController controller = newController();
        Page<DemandeCreditInterne> page = new PageImpl<>(List.of(demande(1L, "client1")));
        doReturn(List.of(new SimpleGrantedAuthority("ROLE_CLIENT"))).when(authentication).getAuthorities();
        when(authentication.getName()).thenReturn("client1");
        when(service.obtenirDemandesDuClient(eq("client1"), any())).thenReturn(page);

        ResponseEntity<Page<DemandeCreditApi>> response =
                controller.obtenirToutesLesDemandes(Pageable.unpaged(), authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().getTotalElements());
    }

    @Test
    void shouldChangeStatut() {
        DemandeCreditController controller = newController();
        when(authentication.getName()).thenReturn("analyste1");
        when(service.changerStatut(eq(1L), eq(StatutDemande.EN_INSTRUCTION), eq("analyste1")))
                .thenReturn(Optional.of(demande(1L, "client1")));

        ChangementStatutApi body = ChangementStatutApi.builder().statut("EN_INSTRUCTION").build();
        ResponseEntity<DemandeCreditApi> response = controller.changerStatut(1L, body, authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void shouldReturn404WhenChangingStatutOfMissingDemande() {
        DemandeCreditController controller = newController();
        when(authentication.getName()).thenReturn("analyste1");
        when(service.changerStatut(eq(999L), eq(StatutDemande.EN_INSTRUCTION), eq("analyste1")))
                .thenReturn(Optional.empty());

        ChangementStatutApi body = ChangementStatutApi.builder().statut("EN_INSTRUCTION").build();
        ResponseEntity<DemandeCreditApi> response = controller.changerStatut(999L, body, authentication);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
}
