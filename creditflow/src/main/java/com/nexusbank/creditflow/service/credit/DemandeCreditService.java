package com.nexusbank.creditflow.service.credit;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.nexusbank.creditflow.isolation.db.DbIsolationManager;
import com.nexusbank.creditflow.service.credit.modele.DemandeCreditInterne;
import com.nexusbank.creditflow.service.credit.modele.StatutDemande;

@Service
public class DemandeCreditService {
    
    private final DbIsolationManager dbIsolationManager;
    private final StatutTransitionValidator statutTransitionValidator;

    public DemandeCreditService(DbIsolationManager dbIsolationManager, StatutTransitionValidator statutTransitionValidator) {
        this.dbIsolationManager = dbIsolationManager;
        this.statutTransitionValidator = statutTransitionValidator;
    }

    public DemandeCreditInterne creerDemande(DemandeCreditInterne demande) {
        
        DemandeCreditInterne demandeAvecStatut = demande.toBuilder()
                .statut(StatutDemande.SOUMISE)
                .build();
        
        return dbIsolationManager.save(demandeAvecStatut);
    }

    public Optional<DemandeCreditInterne> obtenirDemande(Long id) {
        return dbIsolationManager.findById(id);
    }

    public List<DemandeCreditInterne> obtenirToutesLesDemandes() {
        return dbIsolationManager.findAll();
    }

    public Optional<DemandeCreditInterne> changerStatut(Long id, StatutDemande nouveauStatut) {

        DemandeCreditInterne demande = obtenirDemande(id)
        .orElseThrow(() -> new IllegalArgumentException("Demande non trouvée : " + id));

        statutTransitionValidator.valider(demande.getStatut(), nouveauStatut);

        return dbIsolationManager.updateStatut(id, nouveauStatut.name());
    }
}
