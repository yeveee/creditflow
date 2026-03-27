package com.nexusbank.creditflow.api.demande.mappeur;

import java.util.Optional;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.nexusbank.creditflow.api.demande.modele.DemandeCreditApi;
import com.nexusbank.creditflow.commun.mappeur.MappeurReponseApi;
import com.nexusbank.creditflow.service.credit.modele.DemandeCreditInterne;

@Mapper(componentModel = "spring")
public interface MappeurReponseDemande extends MappeurReponseApi<DemandeCreditInterne, DemandeCreditApi> {
    
    @Mapping(target = "statut", expression = "java(interne.getStatut().name())")

    DemandeCreditApi map(DemandeCreditInterne interne);

    default <T> T unwrap(Optional<T> optional) { return optional.orElse(null); }
}
