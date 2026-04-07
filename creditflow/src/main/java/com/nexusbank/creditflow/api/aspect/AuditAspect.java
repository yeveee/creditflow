package com.nexusbank.creditflow.api.aspect;

import java.time.LocalDateTime;

import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.nexusbank.creditflow.isolation.db.DecisionAuditRepository;
import com.nexusbank.creditflow.isolation.db.modele.DecisionAuditEntity;
import com.nexusbank.creditflow.service.credit.modele.DemandeCreditInterne;

@Aspect
@Component
public class AuditAspect {
    
    private final DecisionAuditRepository auditRepository;

    public AuditAspect(DecisionAuditRepository auditRepository) {
        this.auditRepository = auditRepository;
    }

    @AfterReturning(
            pointcut = "execution(* com.nexusbank.creditflow.service.credit.DemandeCreditService.changerStatut(..)) && args(id, nouveauStatut)",
            returning = "result",
            argNames = "id,nouveauStatut,result"
    )
    public void logChangementStatut(Long id, String nouveauStatut, DemandeCreditInterne result){

        String username = SecurityContextHolder.getContext()
        .getAuthentication().getName();
    
    DecisionAuditEntity audit = DecisionAuditEntity.builder()
        .demandeId(id)                          
        .ancienStatut("N/A")                    
        .nouveauStatut(nouveauStatut)            
        .modifiePar(username)                    
        .dateModification(LocalDateTime.now())   
        .build();

        auditRepository.save(audit);
}
}