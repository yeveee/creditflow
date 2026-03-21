package com.nexusbank.creditflow.isolation.db.mappeur;

import java.util.Optional;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.nexusbank.creditflow.isolation.db.modele.DemandeCreditEntity;
import com.nexusbank.creditflow.service.credit.modele.DemandeCreditInterne;
import com.nexusbank.creditflow.service.credit.modele.StatutDemande;

@Mapper(componentModel = "spring")
public interface MappeurReponseDb {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "statut", expression = "java(com.nexusbank.creditflow.service.credit.modele.StatutDemande.valueOf(entity.getStatut()))")
    DemandeCreditInterne map(DemandeCreditEntity entity);

    default <T> Optional<T> wrap(T value) {
        return Optional.ofNullable(value);
    }
}
