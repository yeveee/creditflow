package com.nexusbank.creditflow.service.credit;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.nexusbank.creditflow.isolation.db.DbIsolationManager;
import com.nexusbank.creditflow.isolation.scoring.ScoringIsolationManager;
import com.nexusbank.creditflow.service.credit.modele.DemandeCreditInterne;
import com.nexusbank.creditflow.service.credit.modele.ScoreResultatInterne;
import com.nexusbank.creditflow.service.credit.modele.StatutDemande;

@Service
public class DemandeCreditService {
    
    private final DbIsolationManager dbIsolationManager;
    private final StatutTransitionValidator statutTransitionValidator;
    private final ScoringIsolationManager scoringIsolationManager;

    public DemandeCreditService(DbIsolationManager dbIsolationManager, 
                                StatutTransitionValidator statutTransitionValidator,
                                ScoringIsolationManager scoringIsolationManager) {
        this.dbIsolationManager = dbIsolationManager;
        this.statutTransitionValidator = statutTransitionValidator;
        this.scoringIsolationManager = scoringIsolationManager;
    }

    public DemandeCreditInterne creerDemande(DemandeCreditInterne demande) {
        
        DemandeCreditInterne demandeAvecStatut = demande.toBuilder()
                .statut(StatutDemande.SOUMISE)
                .build();
        
        DemandeCreditInterne saved = dbIsolationManager.save(demandeAvecStatut);

        ScoreResultatInterne score = scoringIsolationManager.calculerScore(
            saved.getNomEmprunteur().orElse("INCONNU"));
        
        DemandeCreditInterne avecScore = saved.toBuilder()
                .scoreCredit(Optional.of(score.getScore()))
                .risqueCredit(Optional.of(score.getRisque()))
                .build();

        return dbIsolationManager.save(avecScore);
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
