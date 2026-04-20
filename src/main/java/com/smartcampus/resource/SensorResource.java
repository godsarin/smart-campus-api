package com.smartcampus.resource;

import com.smartcampus.DataStore;
import com.smartcampus.exception.LinkedResourceNotFoundException;
import com.smartcampus.model.ApiResponse;
import com.smartcampus.model.Room;
import com.smartcampus.model.Sensor;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Path("/sensors")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorResource {

    private static final List<String> VALID_STATUSES = List.of("ACTIVE", "MAINTENANCE", "OFFLINE");

    // GET /api/v1/sensors  
    @GET
    public Response getAllSensors(@QueryParam("type") String type) {
        List<Sensor> sensorList = new ArrayList<>(DataStore.getSensors().values());

        if (type != null && !type.isBlank()) {
            sensorList = sensorList.stream()
                    .filter(s -> s.getType() != null && s.getType().equalsIgnoreCase(type))
                    .collect(Collectors.toList());
        }

        return Response.ok(
                ApiResponse.success("Retrieved " + sensorList.size() + " sensor(s).", sensorList)
        ).build();
    }

    //  POST /api/v1/sensors 
    @POST
    public Response createSensor(Sensor sensor) {
        if (sensor == null || sensor.getId() == null || sensor.getId().isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(ApiResponse.error("Sensor 'id' is required.", null))
                    .build();
        }
        if (sensor.getType() == null || sensor.getType().isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(ApiResponse.error("Sensor 'type' is required.", null))
                    .build();
        }
        if (sensor.getRoomId() == null || sensor.getRoomId().isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(ApiResponse.error("Sensor 'roomId' is required.", null))
                    .build();
        }

        // Default status to ACTIVE if not provided
        if (sensor.getStatus() == null || sensor.getStatus().isBlank()) {
            sensor.setStatus("ACTIVE");
        } else if (!VALID_STATUSES.contains(sensor.getStatus().toUpperCase())) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(ApiResponse.error("Invalid status. Must be one of: ACTIVE, MAINTENANCE, OFFLINE.", null))
                    .build();
        }

        if (DataStore.getSensors().containsKey(sensor.getId())) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(ApiResponse.error("A sensor with id '" + sensor.getId() + "' already exists.", null))
                    .build();
        }

        // Integrity check: verify the referenced room exists
        Room room = DataStore.getRooms().get(sensor.getRoomId());
        if (room == null) {
            throw new LinkedResourceNotFoundException("Room", sensor.getRoomId());
        }

        // Save the sensor
        DataStore.getSensors().put(sensor.getId(), sensor);

        // Register this sensor in the room's sensorIds list
        room.addSensorId(sensor.getId());

        return Response.status(Response.Status.CREATED)
                .entity(ApiResponse.success("Sensor registered successfully.", sensor))
                .build();
    }

    // GET /api/v1/sensors/{sensorId} 
    @GET
    @Path("/{sensorId}")
    public Response getSensorById(@PathParam("sensorId") String sensorId) {
        Sensor sensor = DataStore.getSensors().get(sensorId);
        if (sensor == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(ApiResponse.error("Sensor with id '" + sensorId + "' not found.", null))
                    .build();
        }
        return Response.ok(ApiResponse.success("Sensor found.", sensor)).build();
    }

    // DELETE /api/v1/sensors/{sensorId} 
    @DELETE
    @Path("/{sensorId}")
    public Response deleteSensor(@PathParam("sensorId") String sensorId) {
        Sensor sensor = DataStore.getSensors().get(sensorId);
        if (sensor == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(ApiResponse.error("Sensor with id '" + sensorId + "' not found.", null))
                    .build();
        }

        // Remove sensor from its parent room's sensorIds list
        Room room = DataStore.getRooms().get(sensor.getRoomId());
        if (room != null) {
            room.removeSensorId(sensorId);
        }

        DataStore.getSensors().remove(sensorId);
        DataStore.getSensorReadings().remove(sensorId);

        return Response.ok(
                ApiResponse.success("Sensor '" + sensorId + "' has been removed.", null)
        ).build();
    }

    @Path("/{sensorId}/readings")
    public SensorReadingResource getReadingsResource(@PathParam("sensorId") String sensorId) {
        Sensor sensor = DataStore.getSensors().get(sensorId);
        if (sensor == null) {
            throw new NotFoundException("Sensor with id '" + sensorId + "' not found.");
        }
        return new SensorReadingResource(sensorId);
    }
}
