package com.nexusbank.creditflow.isolation.scoring;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import com.nexusbank.creditflow.commun.mappeur.MappeurUtils;
import com.nexusbank.creditflow.isolation.scoring.mappeur.MappeurReponseScoring;
import com.nexusbank.creditflow.isolation.scoring.modele.ScoreResultatAccesseur;
import com.nexusbank.creditflow.service.credit.modele.ScoreResultatInterne;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

@Component
public class ScoringIsolationManager {

    private final WebClient webClient;
    private final MappeurUtils mappeurUtils;

    public ScoringIsolationManager(WebClient.Builder webClientBuilder, MappeurUtils mappeurUtils) {
        this.webClient = webClientBuilder.baseUrl("http://localhost:8081").build();
        this.mappeurUtils = mappeurUtils;
    }
    @CircuitBreaker(name = "scoring", fallbackMethod = "scoreFallback")

    public ScoreResultatInterne calculerScore(String nomEmprunteur) {
        
        MappeurReponseScoring mappeur = mappeurUtils.getMapper(MappeurReponseScoring.class);

        ScoreResultatAccesseur resultat = webClient.get()
                .uri("/api/score?nom=" + nomEmprunteur)
                .retrieve()
                .bodyToMono(ScoreResultatAccesseur.class)
                .block();
 
                return mappeur.map(resultat);
    }
    private ScoreResultatInterne scoreFallback(String nomEmprunteur, Throwable t) {
        return ScoreResultatInterne.builder()
                .score(500)
                .risque("MOYEN")
                .build();
    }
}