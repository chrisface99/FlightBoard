package com.flightboard.scheduler;

import com.flightboard.client.OpenSkyClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class FlightScheduler {

    private static final Logger log = LoggerFactory.getLogger(FlightScheduler.class);
    private final OpenSkyClient openSkyClient;
    private final SimpMessagingTemplate messagingTemplate;

    private static final double LAMIN = 36.0, LOMIN = -10.0;
    private static final double LAMAX = 60.0, LOMAX = 30.0;

    public FlightScheduler(OpenSkyClient openSkyClient, SimpMessagingTemplate messagingTemplate) {
        this.openSkyClient = openSkyClient;
        this.messagingTemplate = messagingTemplate;
    }

    @Scheduled(fixedDelay = 10_000)
    public void broadcastFlights() {
        openSkyClient.getFlightsInBounds(LAMIN, LOMIN, LAMAX, LOMAX)
            .collectList()
            .subscribe(flights -> {
                log.info("Broadcasting {} flights", flights.size());
                messagingTemplate.convertAndSend("/topic/flights", flights);
            });
    }
}