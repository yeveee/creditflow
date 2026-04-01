package com.nexusbank.creditflow.isolation.scoring.mappeur;

import org.mapstruct.Mapper;

import com.nexusbank.creditflow.commun.mappeur.MappeurReponseAccesseur;
import com.nexusbank.creditflow.isolation.scoring.modele.ScoreResultatAccesseur;
import com.nexusbank.creditflow.service.credit.modele.ScoreResultatInterne;

@Mapper(componentModel = "spring")
public interface MappeurReponseScoring extends MappeurReponseAccesseur<ScoreResultatAccesseur, ScoreResultatInterne> {
    
    ScoreResultatInterne map(ScoreResultatAccesseur accesseur);
}
