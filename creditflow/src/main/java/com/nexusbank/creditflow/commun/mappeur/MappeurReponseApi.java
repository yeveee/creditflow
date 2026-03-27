package com.nexusbank.creditflow.commun.mappeur;

import com.nexusbank.creditflow.commun.modele.ModeleApi;
import com.nexusbank.creditflow.commun.modele.ModeleInterne;

public interface MappeurReponseApi<I extends ModeleInterne, A extends ModeleApi> 
        extends Mappeur<I, A> {
}
