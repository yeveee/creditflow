package com.nexusbank.creditflow.api.demande;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nexusbank.creditflow.api.demande.mappeur.MappeurParametreDemande;
import com.nexusbank.creditflow.api.demande.mappeur.MappeurReponseDemande;
import com.nexusbank.creditflow.api.demande.modele.DemandeCreditApi;
import com.nexusbank.creditflow.service.credit.DemandeCreditService;
import com.nexusbank.creditflow.service.credit.modele.DemandeCreditInterne;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/demandes")
public class DemandeCreditController {

    private final DemandeCreditService service;
    private final MappeurParametreDemande mappeurParametre;
    private final MappeurReponseDemande mappeurReponse;

    public DemandeCreditController(
            DemandeCreditService service,
            MappeurParametreDemande mappeurParametre,
            MappeurReponseDemande mappeurReponse) {
        this.service = service;
        this.mappeurParametre = mappeurParametre;
        this.mappeurReponse = mappeurReponse;
    }

    @PostMapping
    public ResponseEntity<DemandeCreditApi> creerDemande(@Valid @RequestBody DemandeCreditApi demandeApi) {
        DemandeCreditInterne demandeInterne = mappeurParametre.map(demandeApi);
        DemandeCreditInterne saved = service.creerDemande(demandeInterne);
        DemandeCreditApi response = mappeurReponse.map(saved);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DemandeCreditApi> obtenirDemande(@PathVariable Long id) {
        return service.obtenirDemande(id)
                .map(mappeurReponse::map)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<DemandeCreditApi>> obtenirToutesLesDemandes() {
        List<DemandeCreditApi> demandes = service.obtenirToutesLesDemandes().stream()
                .map(mappeurReponse::map)
                .collect(Collectors.toList());
        return ResponseEntity.ok(demandes);
    }
}
