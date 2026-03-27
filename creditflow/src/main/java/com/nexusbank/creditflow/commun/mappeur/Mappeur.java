package com.nexusbank.creditflow.commun.mappeur;

public interface Mappeur<S, T> {
    T map(S source);
}
