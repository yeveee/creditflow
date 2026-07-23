package com.nexusbank.creditflow.isolation.scoring;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import com.nexusbank.creditflow.commun.mappeur.MappeurUtils;
import com.nexusbank.creditflow.isolation.scoring.modele.ScoreResultatAccesseur;
import com.nexusbank.creditflow.service.credit.modele.ScoreResultatInterne;

import reactor.core.publisher.Mono;

public class ScoringIsolationManagerTest {

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private ScoringIsolationManager newManager(ScoreResultatAccesseur stubbedResponse) {
        WebClient.RequestHeadersUriSpec requestHeadersUriSpec = mock(WebClient.RequestHeadersUriSpec.class);
        WebClient.RequestHeadersSpec requestHeadersSpec = mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);
        WebClient webClient = mock(WebClient.class);
        WebClient.Builder webClientBuilder = mock(WebClient.Builder.class);

        when(webClientBuilder.baseUrl(anyString())).thenReturn(webClientBuilder);
        when(webClientBuilder.build()).thenReturn(webClient);
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(ScoreResultatAccesseur.class)).thenReturn(Mono.just(stubbedResponse));

        return new ScoringIsolationManager(webClientBuilder, new MappeurUtils());
    }

    @Test
    void shouldMapExternalScoreResponseToInterneModel() {
        ScoreResultatAccesseur accesseur = ScoreResultatAccesseur.builder()
                .score(720)
                .risque("FAIBLE")
                .build();
        ScoringIsolationManager manager = newManager(accesseur);

        ScoreResultatInterne result = manager.calculerScore("Jean Dupont");

        assertEquals(720, result.getScore());
        assertEquals("FAIBLE", result.getRisque());
    }

    @Test
    void fallbackShouldReturnDefaultMediumRisk() throws Exception {
        ScoringIsolationManager manager = newManager(ScoreResultatAccesseur.builder().build());

        Method fallback = ScoringIsolationManager.class
                .getDeclaredMethod("scoreFallback", String.class, Throwable.class);
        fallback.setAccessible(true);

        ScoreResultatInterne result =
                (ScoreResultatInterne) fallback.invoke(manager, "Jean Dupont", new RuntimeException("service indisponible"));

        assertEquals(500, result.getScore());
        assertEquals("MOYEN", result.getRisque());
    }
}
