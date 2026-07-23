package com.nexusbank.creditflow.api.demande;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nexusbank.creditflow.api.demande.mappeur.MappeurParametreDemande;
import com.nexusbank.creditflow.api.demande.mappeur.MappeurReponseDemande;
import com.nexusbank.creditflow.api.demande.modele.ChangementStatutApi;
import com.nexusbank.creditflow.api.demande.modele.DemandeCreditApi;
import com.nexusbank.creditflow.commun.mappeur.MappeurUtils;
import com.nexusbank.creditflow.service.credit.DemandeCreditService;
import com.nexusbank.creditflow.service.credit.modele.DemandeCreditInterne;
import com.nexusbank.creditflow.service.credit.modele.StatutDemande;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@RestController
@RequestMapping("/api/v1/demandes")
public class DemandeCreditController {

    private final DemandeCreditService service;
    private final MappeurUtils mappeurUtils;

    @Autowired
    public DemandeCreditController(
            DemandeCreditService service,
            MappeurUtils mappeurUtils) {
        this.service = service;
        this.mappeurUtils = mappeurUtils;
    }

    @PostMapping
    public ResponseEntity<DemandeCreditApi> creerDemande(@Valid @RequestBody DemandeCreditApi demandeApi, Authentication authentication) {
        MappeurParametreDemande mappeurParametre = mappeurUtils.getMapper(MappeurParametreDemande.class);
        MappeurReponseDemande mappeurReponse = mappeurUtils.getMapper(MappeurReponseDemande.class);

        DemandeCreditInterne demandeInterne = mappeurParametre.map(demandeApi).toBuilder()
                .clientUsername(Optional.of(authentication.getName()))
                .build();
        DemandeCreditInterne saved = service.creerDemande(demandeInterne);
        DemandeCreditApi response = mappeurReponse.map(saved);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
public ResponseEntity<DemandeCreditApi> obtenirDemande(@PathVariable Long id, Authentication authentication) {
    MappeurReponseDemande mappeurReponse = mappeurUtils.getMapper(MappeurReponseDemande.class);

    return service.obtenirDemande(id)
            .map(demande -> {
                verifierProprietaire(demande, authentication);
                return demande;
            })
            .map(mappeurReponse::map)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
}

private boolean estClient(Authentication authentication) {
    return authentication.getAuthorities().stream()
            .anyMatch(autorite -> autorite.getAuthority().equals("ROLE_CLIENT"));
}

private void verifierProprietaire(DemandeCreditInterne demande, Authentication authentication) {
    if (!estClient(authentication)) {
        return;
    }
    String proprietaire = demande.getClientUsername().orElse(null);
    if (!authentication.getName().equals(proprietaire)) {
        throw new AccessDeniedException("Vous n'êtes pas autorisé à consulter cette demande");
    }
}

    @GetMapping
public ResponseEntity<Page<DemandeCreditApi>> obtenirToutesLesDemandes(Pageable pageable, Authentication authentication) {
    MappeurReponseDemande mappeurReponse = mappeurUtils.getMapper(MappeurReponseDemande.class);

    Page<DemandeCreditInterne> demandes = estClient(authentication)
            ? service.obtenirDemandesDuClient(authentication.getName(), pageable)
            : service.obtenirToutesPaginees(pageable);

    Page<DemandeCreditApi> page = demandes.map(mappeurReponse::map);
    return ResponseEntity.ok(page);
}

@PatchMapping("/{id}/statut")
public ResponseEntity<DemandeCreditApi> changerStatut(
        @PathVariable Long id,
        @Valid @RequestBody ChangementStatutApi body,
        Authentication authentication) {

    MappeurReponseDemande mappeurReponse = mappeurUtils.getMapper(MappeurReponseDemande.class);
    
    // Convert String → enum
    StatutDemande nouveauStatut = StatutDemande.valueOf(body.getStatut());
    
    // Call service (validates transition + persists + audits)
    return service.changerStatut(id, nouveauStatut, authentication.getName())
            .map(mappeurReponse::map)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
}
}
