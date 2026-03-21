package com.nexusbank.creditflow.isolation.db;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.nexusbank.creditflow.isolation.db.mappeur.MappeurParametreDb;
import com.nexusbank.creditflow.isolation.db.mappeur.MappeurReponseDb;
import com.nexusbank.creditflow.isolation.db.modele.DemandeCreditEntity;
import com.nexusbank.creditflow.service.credit.modele.DemandeCreditInterne;

@Component
public class DbIsolationManager {

    private final DemandeCreditRepository repository;
    private final MappeurParametreDb mappeurParametre;
    private final MappeurReponseDb mappeurReponse;

    public DbIsolationManager(
            DemandeCreditRepository repository,
            MappeurParametreDb mappeurParametre,
            MappeurReponseDb mappeurReponse) {
        this.repository = repository;
        this.mappeurParametre = mappeurParametre;
        this.mappeurReponse = mappeurReponse;
    }

    public DemandeCreditInterne save(DemandeCreditInterne demande) {
        DemandeCreditEntity entity = mappeurParametre.map(demande);
        DemandeCreditEntity saved = repository.save(entity);
        return mappeurReponse.map(saved);
    }

    public Optional<DemandeCreditInterne> findById(Long id) {
        return repository.findById(id)
                .map(mappeurReponse::map);
    }

    public List<DemandeCreditInterne> findAll() {
        return repository.findAll().stream()
                .map(mappeurReponse::map)
                .collect(Collectors.toList());
    }
}
