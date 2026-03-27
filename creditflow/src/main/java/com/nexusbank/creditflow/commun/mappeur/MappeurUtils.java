package com.nexusbank.creditflow.commun.mappeur;

import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;

@Component
public class MappeurUtils {
    public <T> T getMapper(Class<T> mappeurClass) {
        return Mappers.getMapper(mappeurClass);
    }
}
