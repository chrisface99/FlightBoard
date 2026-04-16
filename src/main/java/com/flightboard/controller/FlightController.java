package com.flightboard.controller;

import com.flightboard.client.OpenSkyClient;
import com.flightboard.model.FlightState;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST endpoint for on-demand flight queries.
 *
 * We are running on the servlet stack (Tomcat) because
 * @EnableWebSocketMessageBroker requires spring-webmvc.
 * WebFlux is on the classpath only for WebClient (HTTP client).
 * Controllers must therefore return blocking types — call .block()
 * to unwrap the reactive result before returning.
 */
@RestController
@RequestMapping("/api/flights")
public class FlightController {

    private final OpenSkyClient openSkyClient;

    public FlightController(OpenSkyClient openSkyClient) {
        this.openSkyClient = openSkyClient;
    }

    @GetMapping(produces = "application/json")
    public List<FlightState> getFlights(
            @RequestParam(defaultValue = "36.0") double lamin,
            @RequestParam(defaultValue = "-10.0") double lomin,
            @RequestParam(defaultValue = "60.0") double lamax,
            @RequestParam(defaultValue = "30.0") double lomax) {

        return openSkyClient
                .getFlightsInBounds(lamin, lomin, lamax, lomax)
                .collectList()
                .block();
    }
}