package com.nexusbank.creditflow.service.credit.modele;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class DemandeCreditInterne {
    
    private Optional<Long> id;
    private Optional<BigDecimal> montant;
    private Optional<Integer> dureeMois;
    private Optional<String> nomEmprunteur;
    private StatutDemande statut;
    private Optional<LocalDateTime> dateCreation;
}
