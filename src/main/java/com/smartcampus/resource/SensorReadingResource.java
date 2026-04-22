package com.smartcampus.resource;

import com.smartcampus.DataStore;
import com.smartcampus.exception.SensorUnavailableException;
import com.smartcampus.model.ApiResponse;
import com.smartcampus.model.Sensor;
import com.smartcampus.model.SensorReading;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.UUID;

@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorReadingResource {

    private final String sensorId;

    public SensorReadingResource(String sensorId) {
        this.sensorId = sensorId;
    }

    @GET
    public Response getReadings() {
        List<SensorReading> readings = DataStore.getOrCreateReadings(sensorId);
        return Response.ok(
                ApiResponse.success("Retrieved " + readings.size() + " reading(s) for sensor '" + sensorId + "'.", readings)
        ).build();
    }

    @POST
    public Response addReading(SensorReading reading) {
    if (reading == null) {
        return Response.status(Response.Status.BAD_REQUEST)
                .entity(ApiResponse.error("Request body is required.", null))
                .build();
    }

    Sensor sensor = DataStore.getSensors().get(sensorId);

    if (!"ACTIVE".equalsIgnoreCase(sensor.getStatus())) {
        throw new SensorUnavailableException(sensorId, sensor.getStatus());
    }

        if (reading.getId() == null || reading.getId().isBlank()) {
            reading.setId(UUID.randomUUID().toString());
        }
        if (reading.getTimestamp() == 0) {
            reading.setTimestamp(System.currentTimeMillis());
        }

        List<SensorReading> readings = DataStore.getOrCreateReadings(sensorId);
        readings.add(reading);

        sensor.setCurrentValue(reading.getValue());

        return Response.status(Response.Status.CREATED)
                .entity(ApiResponse.success(
                        "Reading recorded and sensor '" + sensorId + "' currentValue updated to " + reading.getValue() + ".",
                        reading
                ))
                .build();
    }

    @GET
    @Path("/{readingId}")
    public Response getReadingById(@PathParam("readingId") String readingId) {
        List<SensorReading> readings = DataStore.getOrCreateReadings(sensorId);
        return readings.stream()
                .filter(r -> r.getId().equals(readingId))
                .findFirst()
                .map(r -> Response.ok(ApiResponse.success("Reading found.", r)).build())
                .orElse(Response.status(Response.Status.NOT_FOUND)
                        .entity(ApiResponse.error("Reading '" + readingId + "' not found for sensor '" + sensorId + "'.", null))
                        .build());
    }
}
