package com.nexusbank.creditflow.commun.mappeur;

import com.nexusbank.creditflow.commun.modele.ModeleApi;
import com.nexusbank.creditflow.commun.modele.ModeleInterne;

public interface MappeurParametreApi<A extends ModeleApi, I extends ModeleInterne> 
    extends Mappeur<A, I>{
}
