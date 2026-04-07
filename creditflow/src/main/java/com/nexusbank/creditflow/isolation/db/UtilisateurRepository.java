package com.nexusbank.creditflow.isolation.db;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nexusbank.creditflow.isolation.db.modele.UtilisateurEntity;

public interface UtilisateurRepository extends JpaRepository<UtilisateurEntity, Long> {
    Optional<UtilisateurEntity> findByUsername(String username);
}
