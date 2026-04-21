package com.smartcampus.exception;

public class SensorUnavailableException extends RuntimeException {

    private final String sensorId;
    private final String status;

    public SensorUnavailableException(String sensorId, String status) {
        super("Sensor '" + sensorId + "' is currently in '" + status
                + "' status and cannot accept new readings. "
                + "Only ACTIVE sensors are able to record data.");
        this.sensorId = sensorId;
        this.status = status;
    }

    public String getSensorId() { return sensorId; }
    public String getStatus() { return status; }
}
