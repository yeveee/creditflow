package com.nexusbank.creditflow.api.demande.mappeur;

import java.util.Optional;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.nexusbank.creditflow.api.demande.modele.DemandeCreditApi;
import com.nexusbank.creditflow.commun.mappeur.MappeurParametreApi;
import com.nexusbank.creditflow.service.credit.modele.DemandeCreditInterne;


@Mapper(componentModel = "spring")
public interface MappeurParametreDemande extends MappeurParametreApi<DemandeCreditApi, DemandeCreditInterne> {
    
    @Mapping(target = "id", expression = "java(java.util.Optional.empty())")
    @Mapping(target = "dateCreation", expression = "java(java.util.Optional.empty())")
    @Mapping(target = "statut", expression = "java(com.nexusbank.creditflow.service.credit.modele.StatutDemande.SOUMISE)")
    DemandeCreditInterne map(DemandeCreditApi api);

    default <T> Optional<T> wrap(T value) {
        return Optional.ofNullable(value);
    }

}
