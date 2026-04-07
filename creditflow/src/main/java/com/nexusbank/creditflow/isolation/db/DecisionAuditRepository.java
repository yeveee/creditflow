package com.nexusbank.creditflow.isolation.db;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nexusbank.creditflow.isolation.db.modele.DecisionAuditEntity;

public interface DecisionAuditRepository extends JpaRepository<DecisionAuditEntity, Long> {
    
}
