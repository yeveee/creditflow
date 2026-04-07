package com.nexusbank.creditflow.isolation.db.modele;

import com.nexusbank.creditflow.commun.modele.ModeleAccesseur;
import com.nexusbank.creditflow.service.credit.modele.Role;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name= "utilisateur")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class UtilisateurEntity implements ModeleAccesseur {


@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
private Long id;

@Column(nullable = false, unique = true)
private String username;

@Column(nullable = false)
private String password;

@Column(nullable = false)
@Enumerated(EnumType.STRING)
private Role role;    

}
