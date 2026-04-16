package com.flightboard.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FlightState {
    private String icao24;
    private String callsign;
    private String originCountry;
    private Double longitude;
    private Double latitude;
    private Double altitude;
    private Boolean onGround;
    private Double velocity;
    private Double trueTrack;
    private Double verticalRate;

    public FlightState() {}

    public FlightState(String icao24, String callsign, String originCountry, Double longitude,
                      Double latitude, Double altitude, Boolean onGround, Double velocity,
                      Double trueTrack, Double verticalRate) {
        this.icao24 = icao24;
        this.callsign = callsign;
        this.originCountry = originCountry;
        this.longitude = longitude;
        this.latitude = latitude;
        this.altitude = altitude;
        this.onGround = onGround;
        this.velocity = velocity;
        this.trueTrack = trueTrack;
        this.verticalRate = verticalRate;
    }

    public String getIcao24() { return icao24; }
    public void setIcao24(String icao24) { this.icao24 = icao24; }

    public String getCallsign() { return callsign; }
    public void setCallsign(String callsign) { this.callsign = callsign; }

    public String getOriginCountry() { return originCountry; }
    public void setOriginCountry(String originCountry) { this.originCountry = originCountry; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getAltitude() { return altitude; }
    public void setAltitude(Double altitude) { this.altitude = altitude; }

    public Boolean getOnGround() { return onGround; }
    public void setOnGround(Boolean onGround) { this.onGround = onGround; }

    public Double getVelocity() { return velocity; }
    public void setVelocity(Double velocity) { this.velocity = velocity; }

    public Double getTrueTrack() { return trueTrack; }
    public void setTrueTrack(Double trueTrack) { this.trueTrack = trueTrack; }

    public Double getVerticalRate() { return verticalRate; }
    public void setVerticalRate(Double verticalRate) { this.verticalRate = verticalRate; }

    public static FlightState fromArray(List<Object> s) {
        FlightState f = new FlightState();
        f.setIcao24(getString(s, 0));
        f.setCallsign(getString(s, 1));
        f.setOriginCountry(getString(s, 2));
        f.setLongitude(getDouble(s, 5));
        f.setLatitude(getDouble(s, 6));
        f.setAltitude(getDouble(s, 7));
        f.setOnGround(s.get(8) instanceof Boolean b && b);
        f.setVelocity(getDouble(s, 9));
        f.setTrueTrack(getDouble(s, 10));
        f.setVerticalRate(getDouble(s, 11));
        return f;
    }

    private static String getString(List<Object> s, int i) {
        return s.get(i) instanceof String v ? v.trim() : null;
    }

    private static Double getDouble(List<Object> s, int i) {
        return s.get(i) instanceof Number n ? n.doubleValue() : null;
    }
}