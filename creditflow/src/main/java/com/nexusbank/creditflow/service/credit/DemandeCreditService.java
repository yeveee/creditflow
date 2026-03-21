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

    public DemandeCreditService(DbIsolationManager dbIsolationManager) {
        this.dbIsolationManager = dbIsolationManager;
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
}
