package com.flightboard.client;

import com.flightboard.model.FlightState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.util.List;

@Component
public class OpenSkyClient {

    private static final Logger log = LoggerFactory.getLogger(OpenSkyClient.class);
    private final WebClient openSkyWebClient;

    public OpenSkyClient(WebClient openSkyWebClient) {
        this.openSkyWebClient = openSkyWebClient;
    }

    public Flux<FlightState> getFlightsInBounds(
            double lamin, double lomin, double lamax, double lomax) {

        return openSkyWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/states/all")
                        .queryParam("lamin", lamin)
                        .queryParam("lomin", lomin)
                        .queryParam("lamax", lamax)
                        .queryParam("lomax", lomax)
                        .build())
                .retrieve()
                .onStatus(status -> !status.is2xxSuccessful(), clientResponse -> {
                    log.error("OpenSky API returned status: {}", clientResponse.statusCode());
                    return clientResponse.bodyToMono(String.class)
                            .flatMap(body -> reactor.core.publisher.Mono.error(
                                    new RuntimeException("API error: " + clientResponse.statusCode() + " - " + body)));
                })
                .bodyToMono(OpenSkyResponse.class)
                .doOnNext(response -> {
                    if (response.getStates() == null || response.getStates().isEmpty()) {
                        log.info("OpenSky API returned 0 flights");
                    } else {
                        log.info("OpenSky API returned {} flights", response.getStates().size());
                    }
                })
                .flatMapMany(response -> {
                    if (response.getStates() == null)
                        return Flux.empty();
                    return Flux.fromIterable(response.getStates())
                            .map(FlightState::fromArray)
                            .filter(f -> f.getLatitude() != null && f.getLongitude() != null);
                })
                .onErrorResume(ex -> {
                    log.warn("OpenSky fetch error: {}", ex.getMessage(), ex);
                    return Flux.empty();
                });
    }

    public static class OpenSkyResponse {
        private Long time;
        private List<List<Object>> states;

        public Long getTime() {
            return time;
        }

        public void setTime(Long time) {
            this.time = time;
        }

        public List<List<Object>> getStates() {
            return states;
        }

        public void setStates(List<List<Object>> states) {
            this.states = states;
        }
    }
}