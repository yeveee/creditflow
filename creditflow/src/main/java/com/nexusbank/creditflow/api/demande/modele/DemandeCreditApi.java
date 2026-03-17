package com.nexusbank.creditflow.api.demande.modele;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DemandeCreditApi {

    private UUID id;

    @NotNull(message = "Le montant est obligatoire")
    @DecimalMin(value = "1000.00", message = "Montant minimum : 1 000 €")
    private BigDecimal montant;

    @NotNull(message = "La durée est obligatoire")
    @Min(value = 6, message = "Durée minimale : 6 mois")
    private Integer dureeMois;

    @NotBlank(message = "Le nom de l'emprunteur est obligatoire")
    private String nomEmprunteur;

    private String statut;

    private LocalDateTime dateCreation;
}