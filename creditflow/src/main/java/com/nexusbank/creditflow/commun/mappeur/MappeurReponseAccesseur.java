package com.nexusbank.creditflow.commun.mappeur;

import com.nexusbank.creditflow.commun.modele.ModeleAccesseur;
import com.nexusbank.creditflow.commun.modele.ModeleInterne;

public interface MappeurReponseAccesseur<A extends ModeleAccesseur, I extends ModeleInterne> 
        extends Mappeur<A, I> {
}
