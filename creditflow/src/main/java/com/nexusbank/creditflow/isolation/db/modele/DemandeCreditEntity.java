package com.nexusbank.creditflow.isolation.db.modele;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.nexusbank.creditflow.commun.modele.ModeleAccesseur;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "demande_credit")
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class DemandeCreditEntity implements ModeleAccesseur {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
 
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal montant;
 
    @Column(name = "duree_mois", nullable = false)
    private Integer dureeMois;
 
    @Column(name = "nom_emprunteur", nullable = false, length = 255)
    private String nomEmprunteur;
 
    @Column(nullable = false, length = 50)
    private String statut;
 
    @Column(name = "date_creation", nullable = false, updatable = false)
    private LocalDateTime dateCreation;
 
    @PrePersist
    protected void onCreate() {
        if (dateCreation == null) {
            dateCreation = LocalDateTime.now();
        }
        if (statut == null) {
            statut = "SOUMISE";
        }
    }
}
