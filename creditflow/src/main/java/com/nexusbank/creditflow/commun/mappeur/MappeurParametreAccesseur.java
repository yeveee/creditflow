package com.nexusbank.creditflow.commun.mappeur;

import com.nexusbank.creditflow.commun.modele.ModeleAccesseur;
import com.nexusbank.creditflow.commun.modele.ModeleInterne;

public interface MappeurParametreAccesseur<I extends ModeleInterne, A extends ModeleAccesseur> 
        extends Mappeur<I, A> {
}
