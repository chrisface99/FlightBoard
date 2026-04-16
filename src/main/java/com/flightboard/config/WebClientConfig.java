package com.flightboard.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.handler.codec.http.HttpHeaderValues;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicReference;

@Configuration
public class WebClientConfig {

    private static final Logger log = LoggerFactory.getLogger(WebClientConfig.class);

    @Value("${opensky.client-id}")     private String clientId;
    @Value("${opensky.client-secret}") private String clientSecret;
    @Value("${opensky.oauth-url}")     private String oauthUrl;

    private final AtomicReference<String> cachedToken = new AtomicReference<>();
    private volatile Instant tokenExpiry = Instant.MIN;

    @Bean
    public WebClient openSkyWebClient() {
        HttpClient httpClient = HttpClient.create().compress(true);

        // Increase buffer size limit from default 256KB to 16MB for flight data
        ExchangeStrategies strategies = ExchangeStrategies.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(16 * 1024 * 1024))
                .build();

        return WebClient.builder()
                .baseUrl("https://opensky-network.org/api")
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .exchangeStrategies(strategies)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .filter(tokenRefreshFilter())
                .build();
    }

    private ExchangeFilterFunction tokenRefreshFilter() {
        return ExchangeFilterFunction.ofRequestProcessor(request ->
            getValidToken()
                .map(token -> ClientRequest.from(request)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .build())
                .defaultIfEmpty(request)
        );
    }

    private Mono<String> getValidToken() {
        if (cachedToken.get() != null && Instant.now().isBefore(tokenExpiry.minusSeconds(30))) {
            return Mono.just(cachedToken.get());
        }
        return fetchNewToken();
    }

    private Mono<String> fetchNewToken() {
        return WebClient.builder().build()
                .post()
                .uri(oauthUrl)
                .header(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded")
                .bodyValue("grant_type=client_credentials"
                         + "&client_id=" + clientId
                         + "&client_secret=" + clientSecret)
                .retrieve()
                .bodyToMono(String.class)
                .map(body -> {
                    try {
                        ObjectMapper mapper = new ObjectMapper();
                        JsonNode root = mapper.readTree(body);
                        String token = root.get("access_token").asText();
                        long expiresIn = root.has("expires_in")
                                ? root.get("expires_in").asLong() : 1800;
                        cachedToken.set(token);
                        tokenExpiry = Instant.now().plusSeconds(expiresIn);
                        log.info("OAuth2 token obtained, expires in {}s", expiresIn);
                        return token;
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to parse OAuth2 token response", e);
                    }
                })
                .onErrorResume(e -> {
                    log.error("Failed to obtain OAuth2 token: {}", e.getMessage());
                    return Mono.empty();
                });
    }
}