package com.nexusbank.creditflow.isolation.db.mappeur;

import java.util.Optional;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.nexusbank.creditflow.commun.mappeur.MappeurParametreAccesseur;
import com.nexusbank.creditflow.isolation.db.modele.DemandeCreditEntity;
import com.nexusbank.creditflow.service.credit.modele.DemandeCreditInterne;

@Mapper(componentModel = "spring")
public interface MappeurParametreDb extends MappeurParametreAccesseur<DemandeCreditInterne, DemandeCreditEntity> {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "dateCreation", ignore = true)
    @Mapping(target = "statut", expression = "java(interne.getStatut().name())")
    DemandeCreditEntity map(DemandeCreditInterne interne);

    default <T> T unwrap(Optional<T> optional) { return optional.orElse(null); }

    
}
