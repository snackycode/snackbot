package com.stattracker.infrastructure.api;

import com.stattracker.application.config.ExternalSecrets;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

/**
 * Configures one {@link WebClient} bean per external game API.
 * API keys are externalized via {@code @Value}.
 */
@Configuration
public class WebClientConfig {

    private static final Logger log = LoggerFactory.getLogger(WebClientConfig.class);

    @Bean
    @Qualifier("riotWebClient")
    public WebClient riotWebClient(ExternalSecrets secrets) {
        return WebClient.builder()
                .baseUrl("https://americas.api.riotgames.com")
                .defaultHeader("X-Riot-Token", secrets.getRiotApiKey())
                .codecs(configurer -> configurer
                        .defaultCodecs()
                        .maxInMemorySize(10 * 1024 * 1024))
                .build();
    }

    @Bean("apexWebClient")
    public WebClient apexWebClient(ExternalSecrets secrets) {
        return WebClient.builder()
                .baseUrl("https://api.mozambiquehe.re")
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader("Authorization", secrets.getApexApiKey())
                .filter(logRequest())
                .build();
    }

    @Bean("fortniteWebClient")
    public WebClient fortniteWebClient(ExternalSecrets secrets) {
        return WebClient.builder()
                .baseUrl("https://fortnite-api.com/v2")
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader("Authorization", secrets.getFortniteApiKey())
                .filter(logRequest())
                .build();
    }

    /**
     * Logging filter for outgoing requests — logs method + URI at DEBUG level.
     */
    private ExchangeFilterFunction logRequest() {
        return ExchangeFilterFunction.ofRequestProcessor(request -> {
            log.debug("API Request: {} {}", request.method(), request.url());
            return Mono.just(request);
        });
    }
}
