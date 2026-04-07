package com.nexusbank.creditflow.isolation.db.modele;

import java.time.LocalDateTime;

import com.nexusbank.creditflow.commun.modele.ModeleAccesseur;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@Entity
@Table(name = "decision_audit")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DecisionAuditEntity implements ModeleAccesseur {
    
    @Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
private Long id;

@Column(nullable = false)
private Long demandeId;

@Column(nullable = false)
private String ancienStatut;

@Column(nullable = false)
private String nouveauStatut;

@Column(nullable = false)
private String modifiePar;

@Column(nullable = false)
private LocalDateTime dateModification;
}
