package com.nexusbank.creditflow.isolation.db;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.nexusbank.creditflow.isolation.db.modele.DemandeCreditEntity;

@Repository
public interface DemandeCreditRepository extends JpaRepository<DemandeCreditEntity, Long> {
    
}