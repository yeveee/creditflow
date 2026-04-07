package com.nexusbank.creditflow.isolation.db;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.nexusbank.creditflow.commun.mappeur.MappeurUtils;
import com.nexusbank.creditflow.isolation.db.mappeur.MappeurParametreDb;
import com.nexusbank.creditflow.isolation.db.mappeur.MappeurReponseDb;
import com.nexusbank.creditflow.isolation.db.modele.DemandeCreditEntity;
import com.nexusbank.creditflow.service.credit.modele.DemandeCreditInterne;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Component
public class DbIsolationManager {

    private final DemandeCreditRepository repository;
    private final MappeurUtils mappeurUtils;

    public DbIsolationManager(
            DemandeCreditRepository repository,
            MappeurUtils mappeurUtils) {
        this.repository = repository;
        this.mappeurUtils = mappeurUtils;
    }

    public DemandeCreditInterne save(DemandeCreditInterne demande) {
        MappeurParametreDb mappeurParametre = mappeurUtils.getMapper(MappeurParametreDb.class);
        MappeurReponseDb mappeurReponse = mappeurUtils.getMapper(MappeurReponseDb.class);
    
        DemandeCreditEntity entity = mappeurParametre.map(demande);
        DemandeCreditEntity saved = repository.save(entity);
        return mappeurReponse.map(saved);
    }

    public Optional<DemandeCreditInterne> findById(Long id) {
        MappeurReponseDb mappeurReponse = mappeurUtils.getMapper(MappeurReponseDb.class);
    
        return repository.findById(id)
                .map(mappeurReponse::map);
    }

    public List<DemandeCreditInterne> findAll() {
        MappeurReponseDb mappeurReponse = mappeurUtils.getMapper(MappeurReponseDb.class);
    
        return repository.findAll().stream()
                .map(mappeurReponse::map)
                .collect(Collectors.toList());
    }

    public Page<DemandeCreditInterne> findAll(Pageable pageable) {
    MappeurReponseDb mappeurReponse = mappeurUtils.getMapper(MappeurReponseDb.class);

    return repository.findAll(pageable)
            .map(mappeurReponse::map);
}

    public Optional<DemandeCreditInterne> updateStatut(Long id, String nouveauStatut) {
        MappeurReponseDb mappeurReponse = mappeurUtils.getMapper(MappeurReponseDb.class);

        return repository.findById(id)
        .map(entity -> {
            entity.setStatut(nouveauStatut);
            return repository.save(entity);
        })
        .map(mappeurReponse::map);
    }
}
